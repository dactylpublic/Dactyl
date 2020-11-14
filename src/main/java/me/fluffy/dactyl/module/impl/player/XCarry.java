package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.item.ItemAir;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XCarry extends Module {
    Setting<Boolean> forceCancel = new Setting<Boolean>("ForceCancel", false);
    public XCarry() {
        super("XCarry", Category.PLAYER);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getPacket() instanceof CPacketCloseWindow) {
            if(forceCancel.getValue()) {
                event.setCanceled(true);
            } else {
                if(isAir(1) && isAir(2) && isAir(3) && isAir(4)) {
                    return;
                }
                event.setCanceled(true);
            }
        }
    }

    private boolean isAir(int slot) {
        return (mc.player.inventoryContainer.getInventory().get(slot).getItem() instanceof ItemAir);
    }
}
