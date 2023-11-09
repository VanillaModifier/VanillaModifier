package org.vanillamodifier.injection.injects;

import org.lwjgl.input.Keyboard;
import org.vanillamodifier.VanillaModifier;
import org.vanillamodifier.annotations.Inject;
import org.vanillamodifier.annotations.Position;
import org.vanillamodifier.events.client.KeyInputEvent;
import org.vanillamodifier.events.client.TickEvent;

public class MinecraftHook {
    @Inject(name = "runTick",desc = "()V",at = @Position(Position.Priority.FRIST))
    public static void runTickHook(){
        VanillaModifier.EVENT_BUS.call(new TickEvent(true));
    }

    @Inject(name = "runTick",desc = "()V",at = @Position(Position.Priority.LAST))
    public static void runTickLastHook(){
        VanillaModifier.EVENT_BUS.call(new TickEvent(false));
    }

    @Inject(name = "runTick",desc = "()V", at = @Position(value = Position.Priority.INVOKE, target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V"))
    public static void onKey() {
        if (Keyboard.getEventKeyState())
            VanillaModifier.EVENT_BUS.call(new KeyInputEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
    }
}
