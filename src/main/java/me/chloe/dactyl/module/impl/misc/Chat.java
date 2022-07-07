package me.chloe.dactyl.module.impl.misc;

import me.chloe.dactyl.event.impl.network.ConnectionEvent;
import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.event.impl.player.MoveEvent;
import me.chloe.dactyl.injection.inj.access.ISPacketChat;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.injection.inj.access.ICPacketChatMessage;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class Chat extends Module {
    public Setting<Boolean> noBorder = new Setting<Boolean>("NoBorder", false);
    public Setting<Boolean> customFont = new Setting<Boolean>("CustomFont", true);
    public Setting<Boolean> chatSuffix = new Setting<Boolean>("Suffix", true);
    public Setting<Boolean> creepyDecrypt = new Setting<Boolean>("Decryptor", true);
    public Setting<Boolean> gradientWatermark = new Setting<Boolean>("CoolWatermark", true);
    public Setting<Boolean> fancySuffix = new Setting<Boolean>("FancySuffix", true, v->chatSuffix.getValue());
    public Setting<String> customChatSuffix = new Setting<String>("CustomSuffix", "Dactyl", v->!fancySuffix.getValue());
    public Setting<Boolean> commands = new Setting<Boolean>("Commands", false, v->chatSuffix.getValue());
    public Setting<Boolean> connectionMessages = new Setting<Boolean>("ConnMsgs", false);
    public Setting<Boolean> connectionWatermark = new Setting<Boolean>("ConnLogo", true, v->connectionMessages.getValue());
    public Setting<Boolean> connectionClogged = new Setting<Boolean>("ConnFilled", false, v->connectionMessages.getValue());
    public Setting<Boolean> afkReply = new Setting<Boolean>("AFKReply", false);
    public Setting<Integer> afkDelay = new Setting<Integer>("AFKDelay", 15, 1, 40, v->afkReply.getValue(), "Delay (in seconds)");
    public Setting<String> afkMessage = new Setting<String>("AFKMessage", "[Dactyl] I am currently AFK.", v->afkReply.getValue());
    public Setting<Boolean> encryptor = new Setting<Boolean>("Encrypt", false);
    public Setting<String> encryptPrefix = new Setting<String>("EncryptPrefix", "**", v->encryptor.getValue());
    public static Chat INSTANCE;
    public Chat() {
        super("Chat", Category.MISC);
        INSTANCE = this;
    }

    private final TimeUtil afkTimer = new TimeUtil();
    private final TimeUtil msgTimer = new TimeUtil();

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        afkTimer.reset();
    }

    @SubscribeEvent
    public void onSendChat(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(encryptor.getValue()) {
                if(event.getPacket() instanceof CPacketChatMessage) {
                    CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();
                    if(packet.getMessage().startsWith(encryptPrefix.getValue())) {
                        event.setCanceled(true);
                        String toEncrypt = packet.getMessage().replaceFirst(Pattern.quote(encryptPrefix.getValue()), "");
                        char[] array = toEncrypt.toCharArray();
                        String xx = "";
                        for(int x : array) {
                            xx += String.valueOf((x+100));
                        }
                        BigInteger bigInteger = new BigInteger(xx);
                        if(("+-"+bigInteger.multiply(new BigInteger("69")).toString()).length() > 256) {
                            return;
                        }
                        mc.player.sendChatMessage("+-"+bigInteger.multiply(new BigInteger("69")).toString());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if (event.getPacket() instanceof SPacketChat && afkTimer.hasPassed(afkDelay.getValue() * 1000) && afkReply.getValue() && (((SPacketChat)event.getPacket()).getChatComponent().getFormattedText()).contains("§d") && ((SPacketChat)event.getPacket()).getChatComponent().getFormattedText().contains(" whispers: ") && msgTimer.hasPassed(10000L)) {
                mc.player.sendChatMessage("/r "+afkMessage.getValue());
                msgTimer.reset();
            }
            if(creepyDecrypt.getValue()) {
                if (event.getPacket() instanceof SPacketChat) {
                    try {
                        SPacketChat packet = (SPacketChat) event.getPacket();
                        String username = StringUtils.stripControlCodes(packet.getChatComponent().getFormattedText().split("> ")[0]);
                        String toDecrypt = StringUtils.stripControlCodes(packet.getChatComponent().getFormattedText().split("> ")[1]);
                        toDecrypt = toDecrypt.replaceAll("[^\\d+-]", "");
                        if (toDecrypt.startsWith("+-")) {
                                toDecrypt = toDecrypt.replace("+-","");
                                BigInteger ilovecreepy = new BigInteger(toDecrypt);
                                BigInteger dogwater = ilovecreepy.divide(new BigInteger("69"));
                                String everyThree = dogwater.toString().replaceAll("...", "$0,");
                                String[] threeSplit = everyThree.split(",");
                                String out = "";
                                for(String x : threeSplit) {
                                    Integer valueInt = Integer.valueOf(x);
                                    out += (char)(valueInt-100);
                                }
                                ((ISPacketChat)packet).setChatComponent(new ChatUtil.FormattedMessage("&6"+username+"> " + out.replaceAll("[^\\p{ASCII}]", "")));
                            }
                        } catch (Exception exception) {
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if(mc.world == null || mc.player == null || event.getName() == null) {
            return;
        }
        if(connectionMessages.getValue()) {
            if (event.getConnectionType() == ConnectionEvent.ConnectionType.LOGOUT) {
                if (!event.getName().equalsIgnoreCase(mc.session.getUsername())) {
                    if(event.getName() != null && !event.getName().equalsIgnoreCase("")) {
                        ChatUtil.printMsg("&7" + event.getName() + " has left the game.", connectionWatermark.getValue(), !connectionClogged.getValue(), 314159);
                    }
                }
            } else {
                if (!event.getName().equalsIgnoreCase(mc.session.getUsername())) {
                    ChatUtil.printMsg("&7" + event.getName() + " has joined the game.", connectionWatermark.getValue(), !connectionClogged.getValue(), 314159);
                }
            }
        }
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
            s = s+ " | "+customChatSuffix.getValue();
        }
        if (s.length() >= 256) s = s.substring(0,256);
        ((ICPacketChatMessage)packetChatMessage).setMessage(s);
    }
}
