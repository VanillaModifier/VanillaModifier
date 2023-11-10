package org.vanillamodifier.injection.injects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.vanillamodifier.VanillaModifier;
import org.vanillamodifier.annotations.Inject;
import org.vanillamodifier.annotations.Position;
import org.vanillamodifier.events.client.MotionUpdateEvent;

public class EntityPlayerSPHook {
    @Inject(name = "onUpdateWalkingPlayer",desc = "()V",at = @Position(Position.Priority.FRIST))
    public static void handleMotion(){
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        VanillaModifier.PLAYER_STATE_MANAGER.x = player.posX;
        VanillaModifier.PLAYER_STATE_MANAGER.y = player.posY;
        VanillaModifier.PLAYER_STATE_MANAGER.z = player.posZ;

        VanillaModifier.PLAYER_STATE_MANAGER.yaw = player.rotationYaw;
        VanillaModifier.PLAYER_STATE_MANAGER.pitch = player.rotationPitch;

        MotionUpdateEvent event = new MotionUpdateEvent(player.rotationYaw,player.rotationPitch,player.posX,player.posY,player.posZ,player.onGround);
        VanillaModifier.EVENT_BUS.call(event);

        player.rotationYaw = event.getYaw();
        player.rotationPitch = event.getPitch();

        player.posX = event.getX();
        player.posY = event.getY();
        player.posZ = event.getZ();

        player.onGround = event.isOnGround();
    }

    @Inject(name = "onUpdateWalkingPlayer",desc = "()V",at = @Position(Position.Priority.LAST))
    public static void handlePostMotion(){
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        MotionUpdateEvent event = new MotionUpdateEvent();
        VanillaModifier.EVENT_BUS.call(event);

        player.rotationYaw = VanillaModifier.PLAYER_STATE_MANAGER.yaw;
        player.rotationPitch = VanillaModifier.PLAYER_STATE_MANAGER.pitch;

        player.posX = VanillaModifier.PLAYER_STATE_MANAGER.x;
        player.posY = VanillaModifier.PLAYER_STATE_MANAGER.y;
        player.posZ = VanillaModifier.PLAYER_STATE_MANAGER.z;

        player.onGround = VanillaModifier.PLAYER_STATE_MANAGER.onGround;

    }
}
