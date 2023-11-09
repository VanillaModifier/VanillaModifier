package org.vanillamodifier.loader;


import org.tinylog.Logger;
import org.vanillamodifier.VanillaModifier;

public class Loader {
    public Loader(){
        Logger.info("Initializing...");
        VanillaModifier.initialize();
    }
}
