package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AltSpeed extends Module {
    public AltSpeed() {
        super("OnGround", Category.MOVEMENT, "zoom");
    }

    public static int ticks;
    public static double moveSpeed;
    private double lastDist;
    private double save;
    public static boolean canStep;
    private boolean stop;
    public static boolean boost = false;


    @Override
    public void onClientUpdate() {
        this.setModuleInfo("YPort");
        if(mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.collidedHorizontally && mc.player.motionY < 1.1034378113E-314 + getOffset()) {
            if (mc.player.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(false));
                return;
            }
            mc.player.motionY = 1.4429571377E-314 + getOffset();
            mc.player.connection.sendPacket(new CPacketPlayer(true));
        }
    }

    public static double getOffset() {
        double d = 0.0;
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            int n = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
            d += (double)(n + 1) * 0.1;
        }
        return d;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (mc.player.onGround) {
            ticks = 2;
        }
        if (MathUtil.round(mc.player.posY - (int)mc.player.posY, 3) == MathUtil.round(0.138, 3)) {
            final EntityPlayerSP thePlayer = mc.player;
            thePlayer.motionY -= 0.08;
            event.setY(event.getY()-0.09316090325960147);
            final EntityPlayerSP thePlayer2 = mc.player;
            thePlayer2.posY -= 0.09316090325960147;
        }
        if (ticks == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f)) {
            ticks = 2;
            moveSpeed = 1.35 * getBaseMoveSpeed() - 0.01;
        }
        else if (ticks == 2) {
            if (stop) {
                moveSpeed = 0.35;
                stop = false;
            }
            moveSpeed *= 0.9;
            ticks = 3;
            if ((mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) && !mc.player.collidedHorizontally) {
                mc.player.motionY = 0.399399995003033;
                event.setY(0.399399995003033);
                moveSpeed *= (boost ? 2.4 : 2.385);
                final EntityPlayerSP thePlayer3 = mc.player;
                thePlayer3.motionY *= -5.0;
                boost = false;
            }
        }
        else if (ticks == 3) {
            ticks = 4;
            final double difference = 0.66 * (lastDist - getBaseMoveSpeed());
            moveSpeed = lastDist - difference;
        }
        else if (mc.world.getCollisionBoxes(mc.player, mc.player.boundingBox.offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
            ticks = 1;
        }
        moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
        final MovementInput movementInput = mc.player.movementInput;
        float forward = movementInput.moveForward;
        float strafe = movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward == 0.0f && strafe == 0.0f) {
            event.setX(0.0);
            event.setZ(0.0);
        }
        else if (forward != 0.0f) {
            if (strafe >= 1.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
                strafe = 0.0f;
            }
            else if (strafe <= -1.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
                strafe = 0.0f;
            }
            if (forward > 0.0f) {
                forward = 1.0f;
            }
            else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double mx = Math.cos(Math.toRadians(yaw + 90.0f));
        final double mz = Math.sin(Math.toRadians(yaw + 90.0f));
        final double motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        final double motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
        event.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
        event.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);
        canStep = true;
        mc.player.stepHeight = 0.6f;
        if (forward == 0.0f && strafe == 0.0f) {
            event.setX(0.0);
            event.setZ(0.0);
        }
        else {
            boolean collideCheck = false;
            if (mc.world.getCollisionBoxes(mc.player, mc.player.boundingBox.expand(0.5, 0.0, 0.5)).size() > 0) {
                collideCheck = true;
            }
            if (forward != 0.0f) {
                if (strafe >= 1.0f) {
                    yaw += ((forward > 0.0f) ? -45 : 45);
                    strafe = 0.0f;
                } else if (strafe <= -1.0f) {
                    yaw += ((forward > 0.0f) ? 45 : -45);
                    strafe = 0.0f;
                }
                if (forward > 0.0f) {
                    forward = 1.0f;
                } else if (forward < 0.0f) {
                    forward = -1.0f;
                }
            }
        }
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier+1));
        }
        return baseSpeed;
    }

    public static void moveEntityStrafe(final double speed, final Entity entity) {
        if (entity != null) {
            final MovementInput movementInput = mc.player.movementInput;
            double forward = movementInput.moveForward;
            double strafe = movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if (forward == 0.0 && strafe == 0.0) {
                entity.motionX = 0.0;
                entity.motionZ = 0.0;
            }
            else {
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += ((forward > 0.0) ? -45 : 45);
                    }
                    else if (strafe < 0.0) {
                        yaw += ((forward > 0.0) ? 45 : -45);
                    }
                    strafe = 0.0;
                    if (forward > 0.0) {
                        forward = 1.0;
                    }
                    else if (forward < 0.0) {
                        forward = -1.0;
                    }
                }
                entity.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
                entity.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
            }
        }
    }




    public enum SpeedMode {
        ONGROUND("OnGround");

        private final String name;
        private SpeedMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
