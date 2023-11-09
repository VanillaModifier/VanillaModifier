package org.vanillamodifier;

import com.cubk.event.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.vanillamodifier.events.internal.AddHookEvent;
import org.vanillamodifier.events.internal.StartHookEvent;
import org.vanillamodifier.injection.InjectManager;
import org.vanillamodifier.injection.injects.GuiInGameHook;
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
            AddHookEvent event = new AddHookEvent();
            EVENT_BUS.call(event);
            if(!event.isCancelled())
                addHooks();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        StartHookEvent event = new StartHookEvent();
        EVENT_BUS.call(event);
        if(!event.isCancelled())
            processInjection();
    }

    private static void addHooks() throws ClassNotFoundException {
        CODE_INJECTOR.addProcessor(MinecraftHook.class, Minecraft.class);
        CODE_INJECTOR.addProcessor(GuiInGameHook.class, GuiIngame.class);
    }

    public static void processInjection(){
        CODE_INJECTOR.process();
    }

}
