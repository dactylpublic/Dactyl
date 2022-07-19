package me.chloe.moonlight.module.impl.player;

import me.chloe.moonlight.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketVehicleMove;

public class Vanish extends Module {
    public Vanish() {
        super("Vanish", Category.PLAYER);
    }

    public Entity entity;

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(mc.player.getRidingEntity() != null) {
            mc.renderGlobal.loadRenderers();
            entity = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
            mc.world.removeEntity(this.entity);
        }
    }

    @Override
    public void onDisable() {
        if(entity == null || mc.world == null || mc.player == null) {
            return;
        }
        entity.isDead = false;
        mc.world.loadedEntityList.add(this.entity);
        mc.player.startRiding(this.entity, true);
        entity = null;
    }


    @Override
    public void onClientUpdate() {
        if(mc.world == null || mc.player == null) {
            return;
        }
        if (this.entity == null && mc.player.getRidingEntity() != null) {
            this.entity = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
            mc.world.removeEntity(this.entity);
        }
        if (this.entity != null) {
            this.entity.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ);
            mc.player.connection.sendPacket(new CPacketVehicleMove(this.entity));
        }
    }
}
