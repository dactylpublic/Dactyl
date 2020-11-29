package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketChatMessage;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Chat extends Module {
    public Setting<Boolean> chatSuffix = new Setting<Boolean>("Suffix", true);
    public Setting<Boolean> fancySuffix = new Setting<Boolean>("FancySuffix", true, v->chatSuffix.getValue());
    public Setting<Boolean> commands = new Setting<Boolean>("Commands", false, v->chatSuffix.getValue());
    public Chat() {
        super("Chat", Category.MISC);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if (event.getPacket() instanceof CPacketChatMessage) {
                if (chatSuffix.getValue()) {
                    doSuffix((CPacketChatMessage) event.getPacket());
                }
            }
        }
    }

    private void doSuffix(CPacketChatMessage packetChatMessage) {
        String s = packetChatMessage.getMessage();
        if (s.startsWith("/") && !commands.getValue()) return;
        if(fancySuffix.getValue()) {
            s = s + " \u23D0 \u1D05\u1D00\u1D04\u1D1B\u028F\u029F";
        } else {
            s = s+ " | Dactyl";
        }
        if (s.length() >= 256) s = s.substring(0,256);
        ((ICPacketChatMessage)packetChatMessage).setMessage(s);
    }
}
