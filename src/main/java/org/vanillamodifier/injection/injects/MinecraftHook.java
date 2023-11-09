package org.vanillamodifier.injection.injects;

import org.vanillamodifier.VanillaModifier;
import org.vanillamodifier.annotations.Inject;
import org.vanillamodifier.annotations.Position;
import org.vanillamodifier.events.client.TickEvent;

public class MinecraftHook {
    @Inject(name = "runTick",desc = "()V",at = @Position(Position.Priority.FRIST))
    public static void runTickHook(){
        VanillaModifier.EVENT_BUS.call(new TickEvent());
    }
}
