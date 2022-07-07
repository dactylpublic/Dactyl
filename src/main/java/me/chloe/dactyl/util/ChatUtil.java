package me.chloe.dactyl.util;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemPiston;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void printMsg(String msg, boolean watermark, boolean unclogged) {
        try {
            if (mc.ingameGUI.getChatGUI() != null) {
                if (unclogged) {
                    mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new FormattedMessage((watermark ? getWatermark() : "") + msg), -1337);
                } else {
                    mc.ingameGUI.getChatGUI().printChatMessage(new FormattedMessage((watermark ? getWatermark() : "") + msg));
                }
            }
        } catch(Exception e) {}
    }

    public static void printMsg(String msg, boolean watermark, boolean unclogged, int chatLineId) {
        try {
            if (mc.ingameGUI == null) {
                return;
            }
            if (mc.ingameGUI.getChatGUI() != null) {
                if (unclogged) {
                    mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new FormattedMessage((watermark ? getWatermark() : "") + msg), chatLineId);
                } else {
                    mc.ingameGUI.getChatGUI().printChatMessage(new FormattedMessage((watermark ? getWatermark() : "") + msg));
                }
            }
        } catch(Exception e) {}
    }

    public static String getWatermark() {
        return "&1<&bDactyl&1>&r ";
    }

    public static class FormattedMessage extends TextComponentBase {
        String msg;

        public FormattedMessage(String msg) {
            this.msg = formatColor(msg);
        }

        private String formatColor(String msg) {
            Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher m = p.matcher(msg);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String replacement = "\u00A7" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);
            return sb.toString();
        }

        @Override
        public String getUnformattedComponentText() {
            return msg;
        }

        @Override
        public ITextComponent createCopy() {
            return new FormattedMessage(msg);
        }
    }

}
