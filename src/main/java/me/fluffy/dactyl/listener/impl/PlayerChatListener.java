package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.action.CommandSendEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.PlayerChatEvent;
import me.fluffy.dactyl.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerChatListener extends Listener {
    public PlayerChatListener() {
        super("PlayerChatListener", "Fired every time the player attempts to send a chat message (Not cancellable)");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketChatMessage) {
                CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();
                PlayerChatEvent e = new PlayerChatEvent(packet.getMessage(), ForgeEvent.Stage.POST);
                MinecraftForge.EVENT_BUS.post(e);
                String cmd = e.getMessage().split(" ")[0].replace(Dactyl.commandManager.getPrefix(), "");
                if(packet.getMessage().startsWith(Dactyl.commandManager.getPrefix()) || Dactyl.commandManager.matchesCommand(cmd)) {
                    if(packet.getMessage().startsWith(Dactyl.commandManager.getPrefix()) && !Dactyl.commandManager.matchesCommand(cmd)) {
                        Dactyl.commandManager.sendHelp();
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(PlayerChatEvent event) {
        if(event.getStage() == ForgeEvent.Stage.POST) {
            String cmd = event.getMessage().split(" ")[0].replace(Dactyl.commandManager.getPrefix(), "");
            if (Dactyl.commandManager.matchesCommand(cmd)) {
                CommandSendEvent e = new CommandSendEvent(cmd, event.getMessage().split(" "));
                MinecraftForge.EVENT_BUS.post(e);
            }
        }
    }
}
