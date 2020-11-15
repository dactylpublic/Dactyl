package me.fluffy.dactyl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class EntityUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int getPing(EntityPlayer p) {
        int ping = 0;
        try {
            ping = (int) mc.getConnection().getPlayerInfo(p.getUniqueID()).getResponseTime();
        } catch (NullPointerException np) {}
        return ping;
    }

    public static String getFacingWithProperCapitals() {
        String directionLabel = mc.getRenderViewEntity().getHorizontalFacing().getName().toLowerCase();
        switch (directionLabel) {
            case "north":
                directionLabel = "North";
                break;

            case "south":
                directionLabel = "South";
                break;

            case "west":
                directionLabel = "West";
                break;

            case "east":
                directionLabel = "East";
                break;
        }
        return directionLabel;
    }

    public static String getRelativeDirection() {
        String directionLabel = "";
        switch (getFacingWithProperCapitals().toLowerCase()) {
            case "north":
                directionLabel = "-Z";
                break;
            case "south":
                directionLabel = "+Z";
                break;
            case "west":
                directionLabel = "-X";
                break;

            case "east":
                directionLabel = "+X";
                break;
        }
        return directionLabel;
    }

    public static boolean isPassiveEntity(Entity entity) {
        if(entity instanceof EntityWolf) {
            return !((EntityWolf)entity).isAngry();
        }
        if(entity instanceof EntityAgeable || entity instanceof EntityTameable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid) {
            return true;
        }
        if(entity instanceof EntityIronGolem) {
            return (((EntityIronGolem)entity).getRevengeTarget() == null);
        }
        return false;
    }

    public static Vec3d interpolateEntity(final Entity entity, final float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }

    public static Vec3d getInterpolatedPos(final Entity entity, final float partialTicks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, partialTicks));
    }

    public static Vec3d getInterpolatedRenderPos(final Entity entity, final float partialTicks) {
        return getInterpolatedPos(entity, partialTicks).subtract(EntityUtil.mc.getRenderManager().viewerPosX, EntityUtil.mc.getRenderManager().viewerPosY, EntityUtil.mc.getRenderManager().viewerPosZ);
    }

    public static Vec3d getInterpolatedRenderPos(final Vec3d vec) {
        return new Vec3d(vec.x, vec.y, vec.z).subtract(EntityUtil.mc.getRenderManager().viewerPosX, EntityUtil.mc.getRenderManager().viewerPosY, EntityUtil.mc.getRenderManager().viewerPosZ);
    }

    public static Vec3d getInterpolatedAmount(final Entity entity, final double x, final double y, final double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedAmount(final Entity entity, final Vec3d vec) {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedAmount(final Entity entity, final float partialTicks) {
        return getInterpolatedAmount(entity, partialTicks, partialTicks, partialTicks);
    }
}
