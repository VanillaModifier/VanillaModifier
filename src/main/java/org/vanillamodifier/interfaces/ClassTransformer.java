package org.vanillamodifier.interfaces;

import org.objectweb.asm.tree.ClassNode;

public interface ClassTransformer {
    void transform(ClassNode classNode);
}
