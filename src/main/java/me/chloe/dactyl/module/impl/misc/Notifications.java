package me.chloe.dactyl.module.impl.misc;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.event.impl.network.PlayerDisconnectEvent;
import me.chloe.dactyl.event.impl.player.UpdatePopEvent;
import me.chloe.dactyl.event.impl.world.EntityAddedEvent;
import me.chloe.dactyl.event.impl.world.EntityRemovedEvent;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Notifications extends Module {
    Setting<NotifMode> modeSetting = new Setting<>("Mode", NotifMode.CHAT);
    Setting<Boolean> visualRange = new Setting<>("VisualRange", true);
    Setting<Boolean> kick = new Setting<>("Kick", true);
    Setting<Boolean> sound = new Setting<>("Sound", false);
    Setting<Boolean> pearls = new Setting<>("Pearls", true);
    Setting<Boolean> totemPops = new Setting<>("Pops", true);
    Setting<Boolean> totemPopWatermark = new Setting<>("PopLogo", true, v->totemPops.getValue());
    Setting<Boolean> totemPopClogged = new Setting<>("PopClogged", false, v -> totemPops.getValue());
    Setting<Boolean> watermark = new Setting<>("Watermark", true, v->modeSetting.getValue() == NotifMode.CHAT);
    Setting<Boolean> unclogged = new Setting<>("Unclogged", true, v->modeSetting.getValue() == NotifMode.CHAT);
    public Notifications() {
        super("Notifications", Category.MISC);
    }

    private final HashMap<String, Long> timeSent = new HashMap<>();
    ConcurrentHashMap<UUID, Integer> uuidMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onPop(UpdatePopEvent event)
    {
        if(mc.player == null || mc.world == null)
            return;

        String message;
        int popCount = event.getPopCount();
        message = popCount > 1 ? String.format("&9%s has popped &6%d totems", event.getUser(), popCount) : String.format("&9%s has popped &6%d totem", event.getUser(), popCount);

        ChatUtil.printMsg(message, totemPopWatermark.getValue(), !totemPopClogged.getValue(), -138592);
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        performUuidMapTimeouts();
        timeSent.entrySet().removeIf(time -> ((System.currentTimeMillis() - time.getValue()) >= 500));
    }

    @SubscribeEvent
    public void onEntityTeleportEvent(EnderTeleportEvent event) {
        if (mc.world == null || mc.player == null || !pearls.getValue())
            return;

        Entity entity = event.getEntity();

        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;

            if (entityPlayer.getName().equals(mc.player.getName()))
                return;

            sendChatNotif(String.format("&d%s's pearl loaded at &f[%d, %d, %d]", entityPlayer.getName(), (int)event.getTargetX(), (int)event.getTargetY(), (int)event.getTargetZ()), 0);
        }
    }

    @SubscribeEvent
    public void onProjectileEntitySpawn(EntityJoinWorldEvent event) {
        if (mc.world == null || mc.player == null || !pearls.getValue())
            return;

        Entity entity = event.getEntity();
        if (entity instanceof EntityEnderPearl)
        {
            if (this.uuidMap.containsKey(entity.getUniqueID()))
                return;

            EntityPlayer closestPlayer = null;

            for (EntityPlayer p : mc.world.playerEntities)
                if (closestPlayer == null || event.getEntity().getDistance(p) < entity.getDistance(closestPlayer))
                    closestPlayer = p;

            if (closestPlayer == null || closestPlayer.getDistance(entity) >= 2.0f || closestPlayer.getName().equals(mc.player.getName()))
                return;

            this.uuidMap.put(entity.getUniqueID(), 200);
            sendChatNotif(String.format("&d%s threw a pearl %s", closestPlayer.getName(), replaceDir(entity.getHorizontalFacing().getName())));
        }
    }

    private void performUuidMapTimeouts()
    {
        this.uuidMap.forEach((name, timeout) -> {
            if (timeout <= 0)
                this.uuidMap.remove(name);
            else
                this.uuidMap.put(name, timeout - 1);
        });
    }

    private String replaceDir(String x)
    {
        if(x.equalsIgnoreCase("west"))
            return "east";

        if(x.equalsIgnoreCase("east"))
            return "west";

        return x;
    }

    @Override
    public void onToggle() {
        timeSent.clear();
    }

    @SubscribeEvent
    public void onPlayerKick(PlayerDisconnectEvent event) throws AWTException {
        if(kick.getValue() && modeSetting.getValue() == NotifMode.POPUP) {
            sendNotification("Kicked from server.", true);
        }
    }

    @SubscribeEvent
    public void onEntityAdded(EntityAddedEvent event) throws AWTException
    {
        if (event.getEntity() == null || event.getEntity().getName().isEmpty() || mc.player == null)
            return;

        String name = event.getEntity().getName();
        if (visualRange.getValue() && event.getEntity() instanceof EntityPlayer && !name.equals(mc.player.getName()))
        {
            if(modeSetting.getValue() == NotifMode.POPUP)
                sendNotification(String.format("%s entered your visual range.", name), true);
            else
                sendNotification(String.format("&9%s entered your visual range.", name), false);
        }
    }

    @SubscribeEvent
    public void onEntityRemoved(EntityRemovedEvent event) throws AWTException
    {
        if(event.getEntity() == null || event.getEntity().getName().isEmpty() || mc.player == null)
            return;

        String name = event.getEntity().getName();
        if (visualRange.getValue() && event.getEntity() instanceof EntityPlayer && !name.equals(mc.player.getName())) {

            if(timeSent.containsKey(String.format("%s left your visual range.", name)) || timeSent.containsKey(String.format("&d%s left your visual range.", name)))
                return;

            if(modeSetting.getValue() == NotifMode.POPUP)
                sendNotification(String.format("%s left your visual range.", name), true);
            else
                sendNotification(String.format("&d%s left your visual range.", name), false);
        }
    }

    private void sendNotification(String message, boolean popup) throws AWTException {
        if(message == null)
            return;

        if(popup)
        {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Dactyl Client");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Dactyl Notification");
            tray.add(trayIcon);
            trayIcon.displayMessage("Dactyl", message, TrayIcon.MessageType.INFO);
        }
        else
            ChatUtil.printMsg(message, watermark.getValue(), unclogged.getValue(), -666);
        if(sound.getValue()) {
            if(mc.player == null)
                return;

            PositionedSoundRecord sound = new PositionedSoundRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 1, mc.player.getPosition());
            mc.getSoundHandler().playSound(sound);
        }
        timeSent.put(message, System.currentTimeMillis());
    }

    private void sendChatNotif(String message) {
        ChatUtil.printMsg(message, watermark.getValue(), unclogged.getValue(), -666);
        if(sound.getValue())
        {
            if(mc.player == null)
                return;

            PositionedSoundRecord sound = new PositionedSoundRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1, 1, mc.player.getPosition());
            mc.getSoundHandler().playSound(sound);
        }
        timeSent.put(message, System.currentTimeMillis());
    }

    private void sendChatNotif(String message, int line) {
        ChatUtil.printMsg(message, watermark.getValue(), unclogged.getValue(), line);
        if(sound.getValue())
        {
            if(mc.player == null)
                return;

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
