package me.chloe.moonlight.util;

import me.chloe.moonlight.Moonlight;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public static String getNameFromUUID(final UUID uuid) {
        try {
            final lookUpName process = new lookUpName(uuid);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getNameFromUUID(final String uuid) {
        try {
            final lookUpName process = new lookUpName(uuid);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        }
        catch (Exception e) {
            return null;
        }
    }

    public static class lookUpName implements Runnable
    {
        private volatile String name;
        private final String uuid;
        private final UUID uuidID;

        public lookUpName(final String input) {
            this.uuid = input;
            this.uuidID = UUID.fromString(input);
        }

        public lookUpName(UUID input) {
            this.uuidID = input;
            this.uuid = input.toString();
        }

        @Override
        public void run() {
            this.name = this.lookUpName();
        }

        public String lookUpName() {
            EntityPlayer player = null;
            if (mc.world != null) {
                player = mc.world.getPlayerEntityByUUID(this.uuidID);
            }
            if (player == null) {
                final String url = "https://api.mojang.com/user/profiles/" + this.uuid.replace("-", "") + "/names";
                try {
                    final String nameJson = IOUtils.toString(new URL(url));
                    if(nameJson.contains(",")) {
                        List<String> names = Arrays.asList(nameJson.split(","));
                        Collections.reverse(names);
                        return names.get(1).replace("{\"name\":\"","").replace("\"","").toString();
                    } else {
                        return nameJson.replace("[{\"name\":\"", "").replace("\"}]", "").toString();
                    }
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                    Moonlight.logger.info("error");
                    return null;
                }
            }
            return player.getName();
        }

        public String getName() {
            return this.name;
        }
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

    public static boolean isInWater(final Entity entity) {
        if (entity == null) {
            return false;
        }
        final double y = entity.posY + 0.01;
        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); ++x) {
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); ++z) {
                final BlockPos pos = new BlockPos(x, (int)y, z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAboveWater(final Entity entity) {
        return isAboveWater(entity, false);
    }

    public static boolean isAboveWater(final Entity entity, final boolean packet) {
        if (entity == null) {
            return false;
        }
        final double y = entity.posY - (packet ? 0.03 : ((entity instanceof EntityPlayer) ? 0.2 : 0.5));
        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); ++x) {
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCloseToAboveWater(final Entity entity, final boolean packet) {
        if (entity == null) {
            return false;
        }
        final double y = entity.posY - (packet ? 0.1 : ((entity instanceof EntityPlayer) ? 0.2 : 0.5));
        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); ++x) {
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
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

    public static boolean isMoving() {
        return (double)mc.player.moveForward != 0.0 || (double)mc.player.moveStrafing != 0.0;
    }
}
