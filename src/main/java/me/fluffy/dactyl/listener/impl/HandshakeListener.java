package me.fluffy.dactyl.listener.impl;

import io.netty.buffer.Unpooled;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketCustomPayload;
import me.fluffy.dactyl.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class HandshakeListener extends Listener {
    public HandshakeListener() {
        super("HandshakeListener", "doesn't send users mod list to the server.");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getPacket() instanceof FMLProxyPacket && !Minecraft.getMinecraft().isSingleplayer()) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof CPacketCustomPayload) {
            CPacketCustomPayload packet = (CPacketCustomPayload) event.getPacket();
            if (packet.getChannelName().equals("MC|Brand")) {
                ((ICPacketCustomPayload)packet).setPacketData(new PacketBuffer(Unpooled.buffer()).writeString("vanilla"));
            }
        }
    }
}
