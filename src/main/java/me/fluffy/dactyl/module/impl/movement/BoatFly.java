package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.event.impl.player.PlayerTravelEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BoatFly extends Module {
    public Setting<Double> speed = new Setting<Double>("Speed", 2.02d, 0.0d, 10.0d);
    public Setting<Double> glideSpeed = new Setting<Double>("GlideSpeed", 1.0d, 0.0d, 10.0d);

    public static BoatFly INSTANCE;
    public BoatFly() {
        super("BoatFly", Category.MOVEMENT);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            if(event.getType() == PacketEvent.PacketType.OUTGOING) {
                if (event.getPacket() instanceof CPacketVehicleMove) {
                    if (mc.player.isRiding() && mc.player.ticksExisted % 4 == 0) {
                        mc.playerController.interactWithEntity(mc.player, mc.player.getRidingEntity(), EnumHand.MAIN_HAND);
                    }
                }
            }
            return;
        } else {
            if ((event.getPacket() instanceof net.minecraft.network.play.client.CPacketPlayer.Rotation || event.getPacket() instanceof net.minecraft.network.play.client.CPacketInput) && mc.player.isRiding()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPacketEvent(PacketEvent event) {
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        if(event.getPacket() instanceof SPacketMoveVehicle) {
            if(mc.player.isRiding()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onTravel(PlayerTravelEvent event) {
        if(mc == null || mc.player == null || mc.player.getRidingEntity() == null) {
            return;
        }
        if(!mc.player.isRiding()) {
            return;
        }
        Entity riding = mc.player.getRidingEntity();
        riding.setNoGravity(true);
        riding.rotationYaw = mc.player.rotationYaw;
        riding.motionY = (-glideSpeed.getValue().floatValue() / 10000.0F);
        double[] dir = MathUtil.directionSpeed(speed.getValue());
        if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
            riding.motionX = dir[0];
            riding.motionY = -(glideSpeed.getValue().floatValue() / 10000.0F);
            riding.motionZ = dir[1];
        }
        if (mc.player.movementInput.jump) {
            riding.motionY = 1.0D;
        } else if (mc.player.movementInput.sneak) {
            riding.motionY = -1.0D;
        }
        event.setCanceled(true);
    }
}
