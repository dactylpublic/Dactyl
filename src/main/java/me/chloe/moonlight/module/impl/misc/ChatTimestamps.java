package me.chloe.moonlight.module.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.setting.Setting;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Calendar;

public class ChatTimestamps extends Module {
    //public Setting<StampFormat> format = new Setting<StampFormat>("Cover", StampFormat.BRACKETS);
    public Setting<Style> styleSetting = new Setting<Style>("Style", Style.BRITISHTWO);
    public Setting<Boolean> space = new Setting<Boolean>("ExtraSpace", true);
    public static ChatTimestamps INSTANCE;
    public ChatTimestamps() {
        super("ChatTimestamps", Category.MISC);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onChatRecieved(ClientChatReceivedEvent event) {
        Calendar rightNow = Calendar.getInstance();
        String decoLeft = "<";
        String decoRight = ">";
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int mins = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        boolean isAfternoon = false;
        if(hour >= 12) isAfternoon = true;
        if(isAfternoon && (hour-12 == 0)) {
            hour = 24;
        }
        String americaOnTop = isAfternoon ? String.valueOf(hour-12) : String.valueOf(hour);
        String british = String.valueOf(hour);

        String timestamp = styleSetting.getValue().toString();
        timestamp = timestamp.replace("H24", british);
        timestamp = timestamp.replace("H12", americaOnTop);
        timestamp = timestamp.replace("mm", (mins < 10 ? "0" : "")+String.valueOf(mins));
        timestamp = timestamp.replace("ss", (seconds < 10 ? "0" : "")+String.valueOf(seconds));

        if (space.getValue()) decoRight += " ";
        String hacker = TextFormatting.BLUE + "" + TextFormatting.RED + "" + TextFormatting.GREEN;
        TextComponentString time = new TextComponentString( hacker + decoLeft + timestamp + decoRight + ChatFormatting.RESET);
        event.setMessage(time.appendSibling(event.getMessage()));
    }

    public enum Style {
        BRITISHONE("H24:mm"),
        BRITISHTWO("H24:mm:ss"),
        AMERICAONE("H12:mm"),
        AMERICATWO("H12:mm:ss");

        private final String name;
        private Style(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /*public enum StampFormat {
        COMPARESIGNS("<>"),
        CURLYBRACES("{}"),
        BRACKETS("[]");

        private final String name;
        private StampFormat(String name) {
            this.name = name;
        }

        public String getLeft() {
            String left = "";
            if(this.toString().equalsIgnoreCase("<>")) {
                left = "<";
            } else if(this.toString().equalsIgnoreCase("{}")) {
                left = "{";
            } else if(this.toString().equalsIgnoreCase("[]")) {
                left = "[";
            }
            return left;
        }

        public String getRight() {
            String right = "";
            if(this.toString().equalsIgnoreCase("<>")) {
                right = ">";
            } else if(this.toString().equalsIgnoreCase("{}")) {
                right = "}";
            } else if(this.toString().equalsIgnoreCase("[]")) {
                right = "]";
            }
            return right;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }*/
}
