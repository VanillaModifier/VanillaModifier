package org.vanillamodifier;

import org.vanillamodifier.injection.InjectManager;
import org.vanillamodifier.plugin.ModAPI;
import org.vanillamodifier.plugin.ModLoader;

public class VanillaModifier {
    public static final InjectManager CODE_INJECTOR = new InjectManager();
    public static final ModAPI API = ModAPI.getApi(pluginLoader -> new ModLoader(), "Loader");
}
