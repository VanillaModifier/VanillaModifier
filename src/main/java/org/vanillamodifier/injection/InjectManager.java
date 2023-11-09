package org.vanillamodifier.injection;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.tinylog.Logger;
import org.vanillamodifier.VanillaModifier;
import org.vanillamodifier.interfaces.IClassTransformer;
import org.vanillamodifier.interfaces.INameTransformer;
import org.vanillamodifier.loader.NativeWrapper;
import org.vanillamodifier.struct.Returnable;
import org.vanillamodifier.util.ASMUtil;
import org.vanillamodifier.util.RandomStringUtil;
import org.vanillamodifier.annotations.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.*;

public class InjectManager implements IClassTransformer {
    private INameTransformer nameTransformer = source -> {
        System.out.println(source);
        return source;
    };

    private final Map<ClassNode,String> classNodes = new ConcurrentHashMap<>();
    private final List<Class<?>> needTransform = new ArrayList<>();
    private final ClassNode wrapper;
    private boolean defineWrapper = true;

    public InjectManager(){
        wrapper = new ClassNode();
        wrapper.visit(V1_8, ACC_PUBLIC, "InjectWrapper_" + RandomStringUtil.getRandomString(10), null, "java/lang/Object", null);
    }

    public void setNameTransformer(INameTransformer nameTransformer) {
        this.nameTransformer = nameTransformer;
    }

    public boolean isDefineWrapper() {
        return defineWrapper;
    }

    public void setDefineWrapper(boolean defineWrapper) {
        this.defineWrapper = defineWrapper;
    }

    public void process(){
        try {
            Map<Class<?>,byte[]> transformMap = new ConcurrentHashMap<>();
            for(Class<?> clazz : needTransform){
                byte[] code = NativeWrapper.getClassBytes(clazz);
                Logger.debug(code.length);
                ClassNode classNode = ASMUtil.toClassNode(code);
                transform(classNode);
                transformMap.put(clazz,ASMUtil.toBytes(classNode));
            }
            if(isDefineWrapper()){
                byte[] bytes = compileWrapper();
                try {
                    NativeWrapper.defineClass(Class.forName("net.minecraft.client.main.Main").getClassLoader(), bytes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            transformMap.forEach(this::redefineClass);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void redefineClass(Class<?> clazz, byte[] bytes) {
        NativeWrapper.redefineClass(clazz,bytes);
    }

    public void addProcessor(Class<?> processorClass, Class<?> target) {
        try {
            Logger.info("Registered hook class {} (for {})",processorClass.getSimpleName(),target.getSimpleName());
            ClassReader classReader = new ClassReader(NativeWrapper.getClassBytes(processorClass));
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            classNodes.put(classNode,target.getName());
            if(!needTransform.contains(target))
                needTransform.add(target);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public byte[] compileWrapper(){
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        try {
            wrapper.accept(writer);
            return writer.toByteArray();
        } catch (Throwable e) {
            writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            return writer.toByteArray();
        }
    }

    public void transform(ClassNode classNode) {
        for (Map.Entry<ClassNode, String> entry : classNodes.entrySet()) {
            ClassNode transformClassNode = entry.getKey();
            String className = entry.getValue().replace(".", "/");

            if (!className.equals(classNode.name)) {
                continue;
            }

            Class<?> wrapperClass = VanillaModifier.API.getLoader().getClassInLoader(transformClassNode.name.replace("/", "."));

            for (Method wrapperMethod : wrapperClass.getDeclaredMethods()) {
                Inject method = wrapperMethod.getAnnotation(Inject.class);
                if (method == null) {
                    continue;
                }

                MethodNode injectMethodNode = transformClassNode.methods.stream()
                        .filter(methodNode -> methodNode.name.equals(wrapperMethod.getName()) && methodNode.desc.equals(ASMUtil.getMethodDescriptor(wrapperMethod)))
                        .findFirst()
                        .orElse(null);

                if (injectMethodNode == null) {
                    continue;
                }

                String newName = method.name() + "$" + RandomStringUtil.getRandomString(10);

                MethodNode injectToTargetMethodNode = new MethodNode(injectMethodNode.access, newName, injectMethodNode.desc, null, null);
                injectToTargetMethodNode.instructions.add(injectMethodNode.instructions);
                injectToTargetMethodNode.access = ACC_PUBLIC | ACC_STATIC;
                wrapper.methods.add(injectToTargetMethodNode);

                for (MethodNode targetMethodNode : classNode.methods) {
                    if (!targetMethodNode.name.equals(method.name()) || !targetMethodNode.desc.equals(method.desc())) {
                        continue;
                    }

                    if (!ASMUtil.getArgs(targetMethodNode.desc).equals(ASMUtil.cleanCallback(ASMUtil.getArgs(injectMethodNode.desc)))) {
                        continue;
                    }

                    int returnCount = 0;
                    int invokeCount = 0;
                    InsnList insnList = new InsnList();

                    Type[] argumentTypes = Type.getMethodType(targetMethodNode.desc).getArgumentTypes();

                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Returnable.class))) {
                        insnList.add(new TypeInsnNode(NEW, ASMUtil.getOwner(Returnable.class)));
                        insnList.add(new InsnNode(DUP));
                        insnList.add(new MethodInsnNode(INVOKESPECIAL, ASMUtil.getOwner(Returnable.class), "<init>", "()V"));
                        insnList.add(new VarInsnNode(ASTORE, argumentTypes.length + 1));
                    }

                    for (int i = 0; i < argumentTypes.length; i++) {
                        Type argumentType = argumentTypes[i];
                        insnList.add(new VarInsnNode(ASMUtil.getLoadOpcode(argumentType.getDescriptor()), i + 1));
                    }

                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Returnable.class))) {
                        insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                    }

                    insnList.add(new MethodInsnNode(INVOKESTATIC, wrapper.name, newName, injectMethodNode.desc));

                    if (injectMethodNode.desc.contains(ASMUtil.getDescriptor(Returnable.class))) {
                        insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Returnable.class), "isCancelled", "()Z"));
                        LabelNode cancelLabel = new LabelNode();
                        insnList.add(new JumpInsnNode(IFEQ, cancelLabel));
                        String returnDesc = ASMUtil.getReturn(targetMethodNode.desc).replace("[", "");
                        switch (returnDesc) {
                            case "V":
                                break;
                            case "Z":
                            case "C":
                            case "B":
                            case "S":
                            case "I":
                            case "J":
                            case "D":
                            case "F":
                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Returnable.class), "getReturnValue" + returnDesc, "()" + returnDesc));
                                break;
                            default:
                                insnList.add(new VarInsnNode(ALOAD, argumentTypes.length + 1));
                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, ASMUtil.getOwner(Returnable.class), "getReturnValue", "()" + Type.getDescriptor(Object.class)));
                                insnList.add(new TypeInsnNode(CHECKCAST, returnDesc));
                                insnList.add(new InsnNode(ASMUtil.getReturnOpcode(returnDesc)));
                                insnList.add(cancelLabel);
                                insnList.add(new FrameNode(F_SAME, 0, null, 0, null));
                        }
                    }

                    switch (method.at().value()) {
                        case FRIST:
                            if (targetMethodNode.instructions.getFirst() == targetMethodNode.instructions.get(0)) {
                                targetMethodNode.instructions.insert(targetMethodNode.instructions.getFirst(), insnList);
                            }
                            break;
                        case LAST:
                            if (targetMethodNode.instructions.get(targetMethodNode.instructions.size() - 3) == targetMethodNode.instructions.get(targetMethodNode.instructions.size() - 3)) {
                                targetMethodNode.instructions.insert(targetMethodNode.instructions.get(targetMethodNode.instructions.size() - 3), insnList);
                            }
                            break;
                        case RETURN:
                            for (AbstractInsnNode targetInstruction : targetMethodNode.instructions) {
                                if (targetInstruction instanceof InsnNode) {
                                    int opcode = targetInstruction.getOpcode();
                                    if (opcode >= IRETURN && opcode <= RETURN) {
                                        returnCount++;
                                        if (method.at().ordinal() == -1) {
                                            targetMethodNode.instructions.insertBefore(targetInstruction, insnList);
                                        } else if (method.at().ordinal() == returnCount) {
                                            targetMethodNode.instructions.insertBefore(targetInstruction, insnList);
                                        }
                                    }
                                }
                            }
                            break;
                        case INVOKE:
                            for (AbstractInsnNode targetInstruction : targetMethodNode.instructions) {
                                if (targetInstruction instanceof MethodInsnNode) {
                                    MethodInsnNode targetMethodInstruction = (MethodInsnNode) targetInstruction;
                                    if (method.at().target().equals("L" + targetMethodInstruction.owner + ";" + targetMethodInstruction.name + targetMethodInstruction.desc)) {
                                        invokeCount++;
                                        if (method.at().ordinal() == -1) {
                                            targetMethodNode.instructions.insert(targetInstruction, insnList);
                                        } else if (method.at().ordinal() == invokeCount) {
                                            targetMethodNode.instructions.insert(targetInstruction, insnList);
                                        }
                                    }
                                }
                            }
                            break;
                        case OVERWRITE:
                            targetMethodNode.instructions = insnList;
                            targetMethodNode.instructions.add(new InsnNode(ASMUtil.getReturnOpcode(targetMethodNode.desc.substring(targetMethodNode.desc.indexOf(")") + 1))));
                            break;
                    }
                }
            }
        }
    }


}
