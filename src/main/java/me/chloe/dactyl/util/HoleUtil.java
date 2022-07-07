package me.chloe.dactyl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class HoleUtil {
    public static final List<BlockPos> holeBlocks = Arrays.asList(new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
    public static final List<BlockPos> surrounded = Arrays.asList(new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));

    private static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isInAnyHole() {
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : holeBlocks) {
            if(mc.world.getBlockState(blockpos.add(bPos)) != null && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != null && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != Blocks.AIR && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != Blocks.LAVA && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != Blocks.FLOWING_LAVA && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != Blocks.FLOWING_WATER && mc.world.getBlockState(blockpos.add(bPos)).getBlock() != Blocks.WATER) {
                size++;
            }
        }
        return (size == 5);
    }

    public static boolean isInHole() {
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : holeBlocks) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        return (size == 5);
    }

    public static boolean isEnemySurrounded(EntityPlayer player) {
        Vec3d playerPos = CombatUtil.interpolateEntity(player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : surrounded) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        return (size == 4);
    }

    public static boolean isSurrounded() {
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : surrounded) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        return (size == 4);
    }

    public static boolean isHole(BlockPos blockpos) {
        int size = 0;
        for(BlockPos bPos : holeBlocks) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        return (size == 5);
    }
}
