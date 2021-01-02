package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;


public class PacketLogger extends Module {
    Setting<Boolean> incoming = new Setting<Boolean>("Incoming", true);
    Setting<Boolean> outgoing = new Setting<Boolean>("Outgoing", true);
    Setting<Boolean> chat = new Setting<Boolean>("Chat", true);
    Setting<Boolean> console = new Setting<Boolean>("Logs", true);
    Setting<Boolean> packetData = new Setting<Boolean>("Data", true);
    public PacketLogger() {
        super("PacketLogger", Category.MISC, "Logs packets, duh (dev)");
    }
    private Packet[] packets;
    @Override
    public void onToggle() {
        this.packets = null;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(incoming.getValue()) {
                if(console.getValue()) {
                    Dactyl.logger.log(Level.INFO, "\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");
                    if (packetData.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Dactyl.logger.log(Level.INFO, StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Dactyl.logger.log(Level.INFO, "}");
                }
                if(chat.getValue()) {
                    ChatUtil.printMsg("\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {", true, false);

                    if (this.packetData.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        ChatUtil.printMsg(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())), true, false);
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    ChatUtil.printMsg("}", true, false);
                }
            }
        } else {
            if (outgoing.getValue()) {
                if (console.getValue()) {
                    Dactyl.logger.log(Level.INFO, "\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");
                    if (packetData.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Dactyl.logger.log(Level.INFO, StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Dactyl.logger.log(Level.INFO, "}");
                }
                if (chat.getValue()) {
                    ChatUtil.printMsg("\2477OUT: \247r" + event.getPacket().getClass().getSimpleName() + " {", true, false);

                    if (this.packetData.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        ChatUtil.printMsg(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())), true, false);
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    ChatUtil.printMsg("}", true, false);
                }
            }
        }
    }
}
