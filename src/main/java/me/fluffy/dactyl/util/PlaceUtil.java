package me.fluffy.dactyl.util;

import me.fluffy.dactyl.module.impl.player.Scaffold;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class PlaceUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final List<Block> invalid = Arrays.asList(Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.ANVIL, Blocks.AIR, (Block)Blocks.FIRE, (Block)Blocks.CHEST, Blocks.ENCHANTING_TABLE, Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST, Blocks.GRAVEL, Blocks.LADDER, Blocks.VINE, (Block)Blocks.BEACON, Blocks.JUKEBOX, (Block)Blocks.ACACIA_DOOR, (Block)Blocks.BIRCH_DOOR, (Block)Blocks.DARK_OAK_DOOR, (Block)Blocks.IRON_DOOR,
            (Block)Blocks.JUNGLE_DOOR, (Block)Blocks.OAK_DOOR, (Block)Blocks.SPRUCE_DOOR, Blocks.IRON_TRAPDOOR, Blocks.TRAPDOOR, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX);


    public static int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock &&
                        !invalid.contains(((ItemBlock)item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }

    public static double[] getExpandCoords(double x, double z, double forward, double strafe, float yaw) {
        BlockPos underPos = new BlockPos(x, mc.player.posY - 1, z);
        Block underBlock = mc.world.getBlockState(underPos).getBlock();
        double xCalc = -999.0D, zCalc = -999.0D;
        double dist = 0.0D;
        double expandDist = Scaffold.INSTANCE.expand.getValue() * 2.0D;
        while (!canPlace(underBlock)) {
            xCalc = x;
            zCalc = z;
            dist++;
            if (dist > expandDist)
                dist = expandDist;
            xCalc += (forward * 0.45D * Math.cos(Math.toRadians((yaw + 90.0F))) + strafe * 0.45D * Math.sin(Math.toRadians((yaw + 90.0F)))) * dist;
            zCalc += (forward * 0.45D * Math.sin(Math.toRadians((yaw + 90.0F))) - strafe * 0.45D * Math.cos(Math.toRadians((yaw + 90.0F)))) * dist;
            if (dist == expandDist)
                break;
            underPos = new BlockPos(xCalc, mc.player.posY - 1, zCalc);
            underBlock = mc.world.getBlockState(underPos).getBlock();
        }
        return new double[] { xCalc, zCalc };
    }

    public static boolean canPlace(Block block) {
        return ((block instanceof net.minecraft.block.BlockAir || (block.equals(Blocks.WATER) || block.equals(Blocks.FLOWING_WATER) || block.equals(Blocks.LAVA) || block.equals(Blocks.FLOWING_LAVA))) && mc.world != null && mc.player != null && Scaffold.INSTANCE.pos != null && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(Scaffold.INSTANCE.pos)).isEmpty());
    }

    public static int getBlockCountHotbar() {
        int blockCount = 0;
        for (int i = 36; i < 45; i++) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock &&
                        !invalid.contains(((ItemBlock)item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }


    public static float[] aimAtLocation(double x, double y, double z, EnumFacing facing) {
        EntitySnowball temp = new EntitySnowball((World)mc.world);
        temp.posX = x + 0.5D;
        temp.posY = y - 2.7035252353D;
        temp.posZ = z + 0.5D;
        return aimAtLocation(temp.posX, temp.posY, temp.posZ);
    }

    public static float[] aimAtLocation(double positionX, double positionY, double positionZ) {
        double x = positionX - mc.player.posX;
        double y = positionY - mc.player.posY;
        double z = positionZ - mc.player.posZ;
        double distance = MathHelper.sqrt(x * x + z * z);
        return new float[] { (float)(Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F, (float)-(Math.atan2(y, distance) * 180.0D / Math.PI) };
    }
}
