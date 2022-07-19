package me.chloe.moonlight.module.impl.player;

import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.injection.inj.access.ICPacketCloseWindow;
import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;
import net.minecraft.item.ItemAir;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class XCarry extends Module {
    Setting<Boolean> forceCancel = new Setting<Boolean>("ForceCancel", false);
    public static XCarry INSTANCE;
    public XCarry() {
        super("XCarry", Category.PLAYER);
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getPacket() instanceof CPacketCloseWindow) {
            CPacketCloseWindow packetCloseWindow = (CPacketCloseWindow)event.getPacket();
            if(forceCancel.getValue()) {
                event.setCanceled(true);
            } else {
                if(isAir(1) && isAir(2) && isAir(3) && isAir(4)) {
                    return;
                }
                event.setCanceled((((ICPacketCloseWindow)packetCloseWindow).getWindowId() == 0));
            }
        }
    }

    private boolean isAir(int slot) {
        return (mc.player.inventoryContainer.getInventory().get(slot).getItem() instanceof ItemAir);
    }
}
