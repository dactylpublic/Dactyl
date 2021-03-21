package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.event.impl.network.PlayerDisconnectEvent;
import me.fluffy.dactyl.event.impl.player.UpdatePopEvent;
import me.fluffy.dactyl.event.impl.world.EntityAddedEvent;
import me.fluffy.dactyl.event.impl.world.EntityRemovedEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Notifications extends Module {
    Setting<NotifMode> modeSetting = new Setting<NotifMode>("Mode", NotifMode.CHAT);
    Setting<Boolean> visualRange = new Setting<Boolean>("VisualRange", true);
    Setting<Boolean> kick = new Setting<Boolean>("Kick", true);
    Setting<Boolean> sound = new Setting<Boolean>("Sound", false);
    Setting<Boolean> pearls = new Setting<Boolean>("Pearls", true);
    Setting<Boolean> totemPops = new Setting<Boolean>("Pops", true);
    Setting<Boolean> totemPopWatermark = new Setting<Boolean>("PopLogo", true, v->totemPops.getValue());
    Setting<Boolean> totemPopClogged = new Setting<Boolean>("PopClogged", false, v->totemPops.getValue());
    Setting<Boolean> watermark = new Setting<Boolean>("Watermark", true, v->modeSetting.getValue() == NotifMode.CHAT);
    Setting<Boolean> unclogged = new Setting<Boolean>("Unclogged", true, v->modeSetting.getValue() == NotifMode.CHAT);
    public Notifications() {
        super("Notifications", Category.MISC);
    }

    private final HashMap<String, Long> timeSent = new HashMap<>();
    ConcurrentHashMap<UUID, Integer> uuidMap = new ConcurrentHashMap<UUID, Integer>();

    @SubscribeEvent
    public void onPop(UpdatePopEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        ChatUtil.printMsg("&9"+event.getUser()+" has popped &6"+String.valueOf(event.getPopCount())+" totem(s).", totemPopWatermark.getValue(), !totemPopClogged.getValue(), -138592);
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        doPearlNotifs();
        timeSent.entrySet().removeIf(time -> ((System.currentTimeMillis() - time.getValue()) >= 500));
    }

    private void doPearlNotifs() {
        if(pearls.getValue()) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderPearl) {
                    EntityPlayer closest = null;
                    for (EntityPlayer p : mc.world.playerEntities) {
                        if (closest == null || entity.getDistance(p) < entity.getDistance(closest)) {
                            closest = p;
                        }
                    }
                    if (closest == null || closest.getDistance(entity) >= 2.0f || this.uuidMap.containsKey(entity.getUniqueID()) || closest.getName().equalsIgnoreCase(mc.player.getName())) {
                        continue;
                    }
                    this.uuidMap.put(entity.getUniqueID(), 200);
                    sendChatNotif("&d" + closest.getName() + " threw a pearl " + replaceDir(entity.getHorizontalFacing().getName()));
                }
            }
        }
        this.uuidMap.forEach((name, timeout) -> {
            if (timeout <= 0) {
                this.uuidMap.remove(name);
            }
            else {
                this.uuidMap.put(name, timeout - 1);
            }
        });
    }

    private String replaceDir(String x) {
        if(x.equalsIgnoreCase("west")) {
            return "east";
        }
        if(x.equalsIgnoreCase("east")) {
            return "west";
        }
        return x;
    }

    @Override
    public void onToggle() {
        timeSent.clear();
    }

    @SubscribeEvent
    public void onPlayerKick(PlayerDisconnectEvent event) throws AWTException {
        if(kick.getValue()) {
            if(modeSetting.getValue() == NotifMode.POPUP) {
                sendNotification("Kicked from server.", true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityAdded(EntityAddedEvent event) throws AWTException {
        if(visualRange.getValue()) {
            if (event.getEntity() instanceof EntityPlayer && event.getEntity() != mc.player) {
                EntityPlayer entityPlayer = (EntityPlayer)event.getEntity();
                if(modeSetting.getValue() == NotifMode.POPUP) {
                    sendNotification(entityPlayer.getName()+" entered your visual range.", true);
                } else {
                    sendNotification("&9"+entityPlayer.getName()+" entered your visual range.", false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemoved(EntityRemovedEvent event) throws AWTException {
        if(event.getEntity() == null || event.getEntity().getName() == null) return;
        if(visualRange.getValue()) {
            if (event.getEntity() instanceof EntityPlayer && event.getEntity() != mc.player) {
                EntityPlayer entityPlayer = (EntityPlayer)event.getEntity();
                if(entityPlayer.getName() == null) {
                    return;
                }
                if(timeSent.containsKey(entityPlayer.getName()+" left your visual range.") || timeSent.containsKey("&d"+entityPlayer.getName()+" left your visual range.")) {
                    return;
                }
                if(modeSetting.getValue() == NotifMode.POPUP) {
                    sendNotification(entityPlayer.getName()+" left your visual range.", true);
                } else {
                    sendNotification("&d"+entityPlayer.getName()+" left your visual range.", false);
                }
            }
        }
    }

    private void sendNotification(String message, boolean popup) throws AWTException {
        if(message == null) {
            return;
        }
        if(popup) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Dactyl Client");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Dactyl Notification");
            tray.add(trayIcon);
            trayIcon.displayMessage("Dactyl", message, TrayIcon.MessageType.INFO);
        } else {
            ChatUtil.printMsg(message, watermark.getValue(), unclogged.getValue(), -666);
        }
        if(sound.getValue()) {
            if(mc.getSoundHandler() == null || mc.player == null) {
                return;
            }
            PositionedSoundRecord sound = new PositionedSoundRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 1, mc.player.getPosition());
            mc.getSoundHandler().playSound(sound);
        }
        timeSent.put(message, System.currentTimeMillis());
    }

    private void sendChatNotif(String message) {
        ChatUtil.printMsg(message, watermark.getValue(), unclogged.getValue(), -666);
        if(sound.getValue()) {
            if(mc.getSoundHandler() == null || mc.player == null) {
                return;
            }
            PositionedSoundRecord sound = new PositionedSoundRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 1, mc.player.getPosition());
            mc.getSoundHandler().playSound(sound);
        }
        timeSent.put(message, System.currentTimeMillis());
    }


    private enum NotifMode {
        CHAT,
        POPUP
    }
}
