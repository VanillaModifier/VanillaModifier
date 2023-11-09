package org.vanillamodifier;

import com.cubk.event.EventManager;
import org.vanillamodifier.events.internal.AddHookEvent;
import org.vanillamodifier.events.internal.StartHookEvent;
import org.vanillamodifier.injection.InjectManager;
import org.vanillamodifier.injection.injects.MinecraftHook;
import org.vanillamodifier.plugin.ModAPI;
import org.vanillamodifier.plugin.ModLoader;

import java.io.File;

public class VanillaModifier {
    public static final InjectManager CODE_INJECTOR = new InjectManager();
    public static final ModAPI API = ModAPI.getApi(pluginLoader -> new ModLoader());
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
        try {
            addHooks();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        processInjection();
    }

    private static void addHooks() throws ClassNotFoundException {
        CODE_INJECTOR.addProcessor(MinecraftHook.class,Class.forName("net.minecraft.client.Minecraft"));
    }

    public static void processInjection(){
        CODE_INJECTOR.process();
    }

}
