package org.vanillamodifier;

import com.cubk.event.EventManager;
import org.vanillamodifier.injection.InjectManager;
import org.vanillamodifier.plugin.ModAPI;
import org.vanillamodifier.plugin.ModLoader;

import java.io.File;

public class VanillaModifier {
    public static final InjectManager CODE_INJECTOR = new InjectManager();
    public static final ModAPI API = ModAPI.getApi(pluginLoader -> new ModLoader(), "Loader");
    public static final EventManager EVENT_BUS = new EventManager();

    public static final File vmDataDir = new File(System.getProperty("user.home"),".vanillamodifier");
    public static final File modDataDir = new File(vmDataDir,"mods");

    public static void initialize()
    {
        if(!vmDataDir.exists())
            vmDataDir.mkdir();
        if(!modDataDir.exists())
            modDataDir.mkdir();
        API.loadAll(modDataDir);

        processInjection();
    }

    public static void processInjection(){
        CODE_INJECTOR.process();
    }

}