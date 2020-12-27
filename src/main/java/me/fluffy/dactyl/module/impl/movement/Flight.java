package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.player.PlayerTravelEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.MathUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Flight extends Module {
    public Setting<Double> speed = new Setting<Double>("Speed", 18.0D, 0.1D, 50.0D);
    public Setting<Double> downSpeed = new Setting<Double>("DownSpeed", 10.0D, 0.1D, 50.0D);
    public Setting<Double> glideSpeed = new Setting<Double>("GlideSpeed", 25.0D, 0.0D, 50.0D);
    public Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", true);
    public Setting<Double> fallSpeed = new Setting<Double>("FallSpeed", 10.0D, 0.0D, 50.0D, v->antiKick.getValue());
    public Flight() {
        super("Flight", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onTravel(PlayerTravelEvent event) {
        if(mc.player == null) {
            return;
        }
        doCreativeFly(event);
    }

    private void doCreativeFly(PlayerTravelEvent event) {
        if (mc.player.movementInput.jump) {
            mc.player.motionY = (speed.getValue()/10);
            event.setCanceled(true);
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        event.setCanceled(true);

        double[] dir = MathUtil.directionSpeed(speed.getValue()/10);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionY = -(glideSpeed.getValue() / 10f);
            mc.player.motionZ = dir[1];
        }
        if(!EntityUtil.isMoving()) {
            if(antiKick.getValue()) {
                mc.player.motionY = -(glideSpeed.getValue() / 10f);
            }
        }

        if (mc.player.movementInput.sneak) {
            mc.player.motionY = -(fallSpeed.getValue()/10);
        }

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }
}
