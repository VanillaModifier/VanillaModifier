package org.vanillamodifier.loader;


import org.vanillamodifier.VanillaModifier;

public class Loader {
    public static void load(){
        new Thread(VanillaModifier::initialize).start();
    }
}
