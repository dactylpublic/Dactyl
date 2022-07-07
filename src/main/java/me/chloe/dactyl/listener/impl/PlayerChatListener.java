package me.chloe.dactyl.listener.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.event.ForgeEvent;
import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.command.impl.IgnoreCommand;
import me.chloe.dactyl.event.impl.action.CommandSendEvent;
import me.chloe.dactyl.event.impl.player.PlayerChatEvent;
import me.chloe.dactyl.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
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
                        event.setCanceled(true);
                    }
                    if(packet.getMessage().startsWith(Dactyl.commandManager.getPrefix()) && Dactyl.commandManager.matchesCommand(cmd)) {
                        event.setCanceled(true);
                    }
                }
            }
        } else if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketChat) {
                SPacketChat packet = (SPacketChat)event.getPacket();
                if(IgnoreCommand.ignoredUsers != null) {
                    String chatMsg = packet.getChatComponent().getUnformattedText();
                    for (String ignored : IgnoreCommand.ignoredUsers) {
                        if(chatMsg.startsWith("<"+ignored) || chatMsg.startsWith(ignored)) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(PlayerChatEvent event) {
        if(event.getStage() == ForgeEvent.Stage.POST) {
            String cmd = event.getMessage().split(" ")[0].replace(Dactyl.commandManager.getPrefix(), "");
            if (event.getMessage().startsWith(Dactyl.commandManager.getPrefix()) && Dactyl.commandManager.matchesCommand(cmd)) {
                CommandSendEvent e = new CommandSendEvent(cmd, event.getMessage().split(" "));
                MinecraftForge.EVENT_BUS.post(e);
            }
        }
    }
}
