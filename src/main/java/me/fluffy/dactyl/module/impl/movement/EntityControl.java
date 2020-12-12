package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityControl extends Module {
    public Setting<Boolean> mountBypass = new Setting<Boolean>("MountBypass", true);
    public static EntityControl INSTANCE;
    public EntityControl() {
        super("EntityControl", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(mountBypass.getValue() ? "Mount" : "Riding");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(mountBypass.getValue()) {
            if(event.getPacket() instanceof CPacketUseEntity) {
                if (((CPacketUseEntity)event.getPacket()).getEntityFromWorld(mc.world) instanceof AbstractChestHorse && ((CPacketUseEntity)event.getPacket()).getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
