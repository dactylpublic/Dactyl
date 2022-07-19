package me.chloe.moonlight.module.impl.player;

import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.module.Module;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiHunger extends Module {
    public AntiHunger() {
        super("AntiHunger", Category.PLAYER);
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo("Sprint");
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            packet.onGround = (mc.player.fallDistance >= 0.0f || mc.playerController.isHittingBlock);
        }
        if (event.getPacket() instanceof CPacketEntityAction) {
            CPacketEntityAction packet = (CPacketEntityAction)event.getPacket();
            if (packet.getAction() == CPacketEntityAction.Action.START_SPRINTING || packet.getAction() == CPacketEntityAction.Action.STOP_SPRINTING) {
                event.setCanceled(true);
            }
        }
    }
}
