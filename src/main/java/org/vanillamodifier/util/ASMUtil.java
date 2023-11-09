package org.vanillamodifier.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.vanillamodifier.struct.Returnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ASMUtil {
    public static int getLoadOpcode(String descriptor) {
        String type = descriptor.replace("[", "");
        int opcode;

        switch (type) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                opcode = Opcodes.ILOAD;
                break;
            case "J":
                opcode = Opcodes.LLOAD;
                break;
            case "D":
                opcode = Opcodes.DLOAD;
                break;
            case "F":
                opcode = Opcodes.FLOAD;
                break;
            default:
                opcode = Opcodes.ALOAD;
                break;
        }

        return opcode;
    }

    public static int getReturnOpcode(String descriptor) {
        String type = descriptor.replace("[", "");
        int opcode;

        switch (type) {
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
                opcode = Opcodes.IRETURN;
                break;
            case "J":
                opcode = Opcodes.LRETURN;
                break;
            case "D":
                opcode = Opcodes.DRETURN;
                break;
            case "F":
                opcode = Opcodes.FRETURN;
                break;
            case "V":
                opcode = Opcodes.RETURN;
                break;
            default:
                opcode = Opcodes.ARETURN;
                break;
        }

        return opcode;
    }

    public static String getDescriptor(Class<?> klass) {
        return Type.getDescriptor(klass);
    }

    public static String getOwner(Class<?> klass) {
        return Type.getDescriptor(klass).substring(1, Type.getDescriptor(klass).length() - 1);
    }

    public static String getArgs(String descriptor) {
        return descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")"));
    }

    public static String getReturn(String descriptor) {
        return descriptor.substring(descriptor.lastIndexOf(")") + 1);
    }

    public static String cleanCallback(String descriptor) {
        return descriptor.replace(getDescriptor(Returnable.class), "");
    }

    public static String getDescriptorForClass(final Class c)
    {
        if(c.isPrimitive())
        {
            if(c==byte.class)
                return "B";
            if(c==char.class)
                return "C";
            if(c==double.class)
                return "D";
            if(c==float.class)
                return "F";
            if(c==int.class)
                return "I";
            if(c==long.class)
                return "J";
            if(c==short.class)
                return "S";
            if(c==boolean.class)
                return "Z";
            if(c==void.class)
                return "V";
            //throw new RuntimeException("Unrecognized primitive "+c);
        }
        if(c.isArray()) return c.getName().replace('.', '/');
        return ('L'+c.getName()+';').replace('.', '/');
    }

    public static String getFieldDescriptor(Field f)
    {
        return getDescriptorForClass(f.getType());
    }

    public static String getConstructorDescriptor(Constructor m)
    {
        String s="(";
        for(final Class c: m.getParameterTypes())
            s+=getDescriptorForClass(c);
        s+=")V";
        return s;
    }
    public static String getMethodDescriptor(Method m)
    {
        String s="(";
        for(final Class c: m.getParameterTypes())
            s+=getDescriptorForClass(c);
        s+=')';
        return s+getDescriptorForClass(m.getReturnType());
    }

    public static ClassNode toClassNode(byte[] bytes) {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        return classNode;
    }

    public static byte[] toBytes(ClassNode classNode) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        return classWriter.toByteArray();
    }
}
