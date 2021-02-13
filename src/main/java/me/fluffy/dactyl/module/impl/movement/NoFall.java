package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {
    public Setting<NFMode> modeSetting = new Setting<NFMode>("Mode", NFMode.PACKET);
    public NoFall() {
        super("NoFall", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if (!(mc.player.fallDistance > 3.0f)) return;
        if(event.getType() == PacketEvent.PacketType.OUTGOING && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = (CPacketPlayer)event.getPacket();
            switch(modeSetting.getValue()) {
                case PACKET:
                    ((ICPacketPlayer)packet).setOnGround(true);
                    break;
                case FUTURE:
                    ((ICPacketPlayer)packet).setY(mc.player.posY + 0.1d);
                    break;
                case PEDRO:
                    mc.player.onGround = true;
                    mc.player.capabilities.isFlying = true;
                    mc.player.capabilities.allowFlying = true;
                    ((ICPacketPlayer)packet).setOnGround(false);
                    mc.player.velocityChanged = true;
                    mc.player.capabilities.isFlying = false;
                    mc.player.jump();
                    break;
            }
        }
    }


    private enum NFMode {
        PACKET,
        FUTURE,
        PEDRO
    }
}
