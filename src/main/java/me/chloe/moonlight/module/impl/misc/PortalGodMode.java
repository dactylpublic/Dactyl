package me.chloe.moonlight.module.impl.misc;

import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.module.Module;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PortalGodMode extends Module {
    public PortalGodMode() {
        super("PortalGodMode", Category.MISC);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketConfirmTeleport) {
            event.setCanceled(true);
        }
    }
}
