package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.PlayerChatEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoDuelWin extends Module {
    public Setting<KillCommand> command = new Setting<KillCommand>("Command", KillCommand.KILL);

    public static AutoDuelWin INSTANCE;
    public AutoDuelWin() {
        super("AutoDuelWin", Category.MISC);
        INSTANCE = this;
    }

    private boolean sentDuel = false;
    private boolean killed = false;
    private boolean disconnected = false;

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketChat) {
                if(!sentDuel) {
                    SPacketChat chatPacket = (SPacketChat)event.getPacket();
                    if(chatPacket.getChatComponent() == null || chatPacket.getChatComponent().getUnformattedText() == null) {
                        return;
                    }
                    if(chatPacket.getChatComponent().getUnformattedText().contains("Duel request sent to")) {
                        sentDuel = true;
                    }
                }
            }
            if(event.getPacket() instanceof SPacketTitle) {
                SPacketTitle titlePacket = (SPacketTitle)event.getPacket();
                if(titlePacket.getMessage() == null || titlePacket.getMessage().getUnformattedText() == null) {
                    return;
                }
                if(titlePacket.getMessage().getUnformattedText().contains("GO")) {
                    if(sentDuel && killed && !disconnected) {
                        FMLClientHandler.instance().getClientToServerNetworkManager().closeChannel((ITextComponent)new TextComponentString("Duel won."));
                        disconnected = true;
                        this.toggle();
                    }
                }
            }
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null) {
            return;
        }
        if(sentDuel) {
            if(!killed) {
                mc.player.sendChatMessage((command.getValue() == KillCommand.KILL ? "/kill" : "/suicide"));
                killed = true;
            }
        }
    }


    @Override
    public void onDisable() {
        reset();
    }

    private void reset() {
        sentDuel = false;
        killed = false;
    }

    private enum KillCommand {
        KILL,
        SUICIDE
    }
}
