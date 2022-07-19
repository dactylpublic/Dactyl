package me.chloe.moonlight.module.impl.movement;

import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MovementInput;

public class EntitySpeed extends Module {
    public Setting<Double> speed = new Setting<Double>("Speed", 2.0d, 0.1d, 5.0d);
    public EntitySpeed() {
        super("EntitySpeed", Category.MOVEMENT);
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(String.valueOf(this.speed.getValue()));
        if(mc.player == null || mc.player.getRidingEntity() == null) {
            return;
        }
        MovementInput movementInput = mc.player.movementInput;
        double d1 = movementInput.moveForward;
        double d2 = movementInput.moveStrafe;
        float f = mc.player.rotationYaw;
        if (d1 == 0.0D && d2 == 0.0D) {
            (mc.player.getRidingEntity()).motionX = 0.0D;
            (mc.player.getRidingEntity()).motionZ = 0.0D;
        } else {
            if (d1 != 0.0D) {
                if (d2 > 0.0D) {
                    f += ((d1 > 0.0D) ? -45 : 45);
                } else if (d2 < 0.0D) {
                    f += ((d1 > 0.0D) ? 45 : -45);
                }
                d2 = 0.0D;
                if (d1 > 0.0D) {
                    d1 = 1.0D;
                } else if (d1 < 0.0D) {
                    d1 = -1.0D;
                }
            }
            (mc.player.getRidingEntity()).motionX = d1 * this.speed.getValue() * Math.cos(Math.toRadians((f + 90.0F))) + d2 * this.speed.getValue() * Math.sin(Math.toRadians((f + 90.0F)));
            (mc.player.getRidingEntity()).motionZ = d1 * this.speed.getValue() * Math.sin(Math.toRadians((f + 90.0F))) - d2 * this.speed.getValue() * Math.cos(Math.toRadians((f + 90.0F)));
            if (mc.player.getRidingEntity() instanceof EntityMinecart) {
                ((EntityMinecart) mc.player.getRidingEntity()).setVelocity(d1 * this.speed.getValue() * Math.cos(Math.toRadians((f + 90.0F))) + d2 * this.speed.getValue() * Math.sin(Math.toRadians((f + 90.0F))), ((EntityMinecart) mc.player.getRidingEntity()).motionY, d1 * this.speed.getValue() * Math.sin(Math.toRadians((f + 90.0F))) - d2 * this.speed.getValue() * Math.cos(Math.toRadians((f + 90.0F))));
            }
        }
    }
}
