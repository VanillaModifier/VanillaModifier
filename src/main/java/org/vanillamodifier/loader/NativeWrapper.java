package org.vanillamodifier.loader;

public class NativeWrapper {
    public static native int redefineClass(Class<?> clazz, byte[] array);
    public static native byte[] getClassBytes(Class<?> clazz);
    public static native Class<?> defineClass(ClassLoader loader,byte[] array);
}
