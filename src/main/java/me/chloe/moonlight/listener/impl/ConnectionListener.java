package me.chloe.moonlight.listener.impl;

import com.google.common.base.Strings;
import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.event.impl.network.ConnectionEvent;
import me.chloe.moonlight.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Objects;

public class ConnectionListener extends Listener {
    public ConnectionListener() {
        super("ConnectionListener", "Handles events for players logging in and logging out.");
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        try {
            if (event.getType() == PacketEvent.PacketType.INCOMING) {
                if (event.getPacket() instanceof SPacketPlayerListItem) {
                    SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
                    if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet.getAction())) {
                        return;
                    }
                    packet.getEntries().stream().filter(Objects::nonNull).filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null).forEach(data -> {
                        switch (packet.getAction()) {
                            case ADD_PLAYER:
                                if (data.getProfile().getId() != mc.session.getProfile().getId()) {
                                    if (data != null && data.getProfile() != null && data.getProfile().getName() != null) {
                                        MinecraftForge.EVENT_BUS.post(new ConnectionEvent(data.getProfile().getName(), ConnectionEvent.ConnectionType.LOGIN));
                                    }
                                }
                                break;
                            case REMOVE_PLAYER:
                                if (data != null && data.getProfile() != null && data.getProfile().getId() != null && data.getProfile().getId() != mc.session.getProfile().getId()) {
                                    if (data != null && data.getProfile() != null) {
                                        Entity entity = mc.world.getPlayerEntityByUUID(data.getProfile().getId());
                                        MinecraftForge.EVENT_BUS.post(new ConnectionEvent((entity != null ? entity.getName() : null), ConnectionEvent.ConnectionType.LOGOUT));
                                    }
                                }
                        }
                    });
                }
            }
        } catch(Exception exception) {}
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Moonlight.moduleManager.onLogout();
    }
}
