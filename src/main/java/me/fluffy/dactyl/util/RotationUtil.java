package me.fluffy.dactyl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final long DEFAULT_N = 1000000000L;

    private static float yaw;
    private static float pitch;

    public static void updateRotations() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
    }

    public static void restoreRotations() {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;
    }

    public static void setPlayerRotations(final float yaw, final float pitch) {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;
    }

    public void setPlayerYaw(final float yaw) {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
    }

    public static boolean yawReached(float currentYaw, float targetYaw) {
        if(targetYaw < 0) {
            if(currentYaw <= targetYaw) {
                return true;
            }
        } else {
            if(currentYaw >= targetYaw) {
                return true;
            }
        }
        return false;
    }


    public static float checkYaw(float currentYaw, float targetYaw, float stepAmt) {
        if((currentYaw == targetYaw-stepAmt) || (currentYaw == targetYaw+stepAmt)) {
            return targetYaw;
        }
        return currentYaw;
    }

    public static float properYawStep(float currentYaw, float targetYaw, float stepAmt) {
        float yawTicks = currentYaw;
        if (targetYaw >= 0) {
            yawTicks += stepAmt;
            if (yawTicks >= targetYaw) {
                return (float) targetYaw;
            } else {
                return (float) yawTicks;
            }
        } else {
            // negative
            yawTicks -= stepAmt;
            if (yawTicks <= targetYaw) {
                return (float) targetYaw;
            } else {
                return (float) yawTicks;
            }
        }
    }

    public static float doYawStep(float currentYaw, float targetYaw, float stepAmt) {
        float addedYaw = currentYaw;
        if(targetYaw >= addedYaw) {
            addedYaw+=stepAmt;
        } else if(targetYaw <= addedYaw){
            addedYaw-=stepAmt;
        }
        if(targetYaw < 0) {
            if(addedYaw <= targetYaw) {
                return targetYaw;
            }
        } else {
            if(addedYaw >= targetYaw) {
                return targetYaw;
            }
        }
        return addedYaw;
    }
}
