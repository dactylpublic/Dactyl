package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.world.BlockPushEvent;
import me.fluffy.dactyl.event.impl.world.CollisionAppliedEvent;
import me.fluffy.dactyl.event.impl.world.WaterPushEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {
    Setting<Boolean> explosions = new Setting<Boolean>("Explosions", true);
    Setting<Boolean> fishingHooks = new Setting<Boolean>("FishingHooks", true);
    Setting<Boolean> noPush = new Setting<Boolean>("NoPush", true);
    public Velocity() {
        super("Velocity", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onBlockPush(BlockPushEvent event) {
        if(noPush.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWaterPush(WaterPushEvent event) {
        if(noPush.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCollision(CollisionAppliedEvent event) {
        if(noPush.getValue()) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (mc.player == null) {
            return;
        }

        if (event.getPacket() instanceof SPacketEntityStatus && fishingHooks.getValue()) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 31) {
                Entity entity = packet.getEntity(Minecraft.getMinecraft().world);
                if (entity != null && entity instanceof EntityFishHook) {
                    EntityFishHook fishHook = (EntityFishHook) entity;
                    if (fishHook.caughtEntity == Minecraft.getMinecraft().player) {
                        event.setCanceled(true);
                    }
                }
            }
        }
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() == mc.player.getEntityId()) {
                event.setCanceled(true);
            }
        }
        if (event.getPacket() instanceof SPacketExplosion && explosions.getValue()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo("Force");
    }
}
