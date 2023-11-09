package org.vanillamodifier.injection.injects;

import net.minecraft.client.gui.ScaledResolution;
import org.vanillamodifier.VanillaModifier;
import org.vanillamodifier.annotations.Inject;
import org.vanillamodifier.annotations.Position;
import org.vanillamodifier.events.client.RenderGameOverlayEvent;

public class GuiInGameHook {
    @Inject(name="renderTooltip",desc = "(Lnet/minecraft/client/gui/ScaledResolution;F)V", at=@Position(value=Position.Priority.FRIST))
    public static void handleRender2D(ScaledResolution sr, float partialTicks) {
        VanillaModifier.EVENT_BUS.call(new RenderGameOverlayEvent(sr,partialTicks));
    }
}
