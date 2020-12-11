package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.network.ConnectionEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ConnectionListener extends Listener {
    public ConnectionListener() {
        super("ConnectionListener", "Handles events for players logging in and logging out.");
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketPlayerListItem) {
                SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
                if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
                    for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                        if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                            if(playerData != null && playerData.getProfile() != null && playerData.getProfile().getName() != null) {
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(playerData.getProfile().getName(), ConnectionEvent.ConnectionType.LOGIN));
                            }
                        }
                    }
                }
                if (packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                    for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                        if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                            if(playerData != null && playerData.getProfile() != null && playerData.getProfile().getName() != null) {
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(playerData.getProfile().getName(), ConnectionEvent.ConnectionType.LOGOUT));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Dactyl.moduleManager.onLogout();
    }
}
