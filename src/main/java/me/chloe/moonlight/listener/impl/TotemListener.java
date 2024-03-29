package me.chloe.moonlight.listener.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.event.impl.player.UpdatePopEvent;
import me.chloe.moonlight.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TotemListener extends Listener {
    private final Minecraft mc = Minecraft.getMinecraft();

    public TotemListener() {
        super("TotemListener", "Handles totem pops.");
    }

    @SubscribeEvent
    public void onUpdateClient(TickEvent.ClientTickEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        for(EntityPlayer player : mc.world.playerEntities) {
            if(player.getHealth() <= 0) {
                if(Moonlight.totemManager.containsKey(player.getName())) {
                    Moonlight.totemManager.clearUser(player.getName());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.world == null || mc.player == null) {
            return;
        }
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketEntityStatus) {
                SPacketEntityStatus packetEntityStatus = (SPacketEntityStatus)event.getPacket();
                if(packetEntityStatus.getOpCode() == 35) {
                    if(packetEntityStatus.getEntity(mc.world) != null && packetEntityStatus.getEntity(mc.world) instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer)packetEntityStatus.getEntity(mc.world);
                        Moonlight.totemManager.updatePopped(player.getName());
                    }
                }
            }
        }
    }

    //@SubscribeEvent
    //public void onDeath(LivingDeathEvent event) {
    //    if(event.getEntity() instanceof EntityPlayer) {
    //        EntityPlayer player = (EntityPlayer)event.getEntity();
    //        Dactyl.totemManager.clearUser(player.getName());
    //    }
    //}

    @SubscribeEvent
    public void onPop(UpdatePopEvent event) {
        // TODO: player popped totem, send chat message or some shit
    }
}
