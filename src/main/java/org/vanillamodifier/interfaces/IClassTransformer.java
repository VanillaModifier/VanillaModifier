package org.vanillamodifier.interfaces;

import org.objectweb.asm.tree.ClassNode;

public interface IClassTransformer {
    void transform(ClassNode classNode);
}
