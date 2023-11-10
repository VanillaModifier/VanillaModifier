package org.vanillamodifier;

import com.cubk.event.EventManager;
import org.tinylog.Logger;
import org.vanillamodifier.events.internal.AddHookEvent;
import org.vanillamodifier.events.internal.StartHookEvent;
import org.vanillamodifier.injection.InjectManager;
import org.vanillamodifier.injection.injects.*;
import org.vanillamodifier.plugin.ModAPI;
import org.vanillamodifier.plugin.ModLoader;
import org.vanillamodifier.util.PlayerStateManager;

import java.io.File;

public class VanillaModifier {
    public static final InjectManager CODE_INJECTOR = new InjectManager();
    public static final ModAPI API = ModAPI.getApi(pluginLoader -> new ModLoader());
    public static final EventManager EVENT_BUS = new EventManager();
    public static final PlayerStateManager PLAYER_STATE_MANAGER = new PlayerStateManager();

    public static final File vmDataDir = new File(System.getProperty("user.home"),".vanillamodifier");
    public static final File modDataDir = new File(vmDataDir,"mods");

    public static void initialize()
    {
        if(!vmDataDir.exists())
            vmDataDir.mkdir();
        if(!modDataDir.exists())
            modDataDir.mkdir();
        Logger.info("Loading plugins...");
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
        Logger.info("Registering event hooks...");
        CODE_INJECTOR.addProcessor(MinecraftHook.class, getClazz("net.minecraft.client.Minecraft"));
        CODE_INJECTOR.addProcessor(GuiInGameHook.class, getClazz("net.minecraft.client.gui.GuiIngame"));
        CODE_INJECTOR.addProcessor(EntityPlayerSPHook.class, getClazz("net.minecraft.client.entity.EntityPlayerSP"));
    }

    public static Class<?> getClazz(String name){
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                return API.getLoader().getClassInLoader(name);
            }
        }
    }

    public static void processInjection(){
        CODE_INJECTOR.process();
    }

}
