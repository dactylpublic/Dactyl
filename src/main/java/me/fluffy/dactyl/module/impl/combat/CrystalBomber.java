package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.misc.MiningTweaks;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class CrystalBomber extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 50, 1, 500);
    public Setting<Integer> placeDelay = new Setting<Integer>("ObiDelay", 150, 1, 500);
    public Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 6.0D, 1.0D, 10.0D);
    public Setting<Boolean> oneBlock = new Setting<Boolean>("1.13+", false);
    public Setting<Boolean> swing = new Setting<Boolean>("Swing", true);
    public Setting<SwapMode> swapModeSetting = new Setting<SwapMode>("SwapMode", SwapMode.NORMAL);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    public CrystalBomber() {
        super("CrystalBomber", Category.COMBAT);
    }

    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil placeTimer = new TimeUtil();
    private final TimeUtil checkTimer = new TimeUtil();
    private final TimeUtil justPlacedTimer = new TimeUtil();
    private EntityPlayer bestTarget;
    private boolean hasAlreadySwitched = false;

    @Override
    public void onDisable() {
        resetCrystalBomber();
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            resetCrystalBomber();
            timer.reset();
            return;
        }
        if(!(MiningTweaks.INSTANCE.isEnabled())) {
            toggleWithReason("InstaMine is not enabled!");
            return;
        }
        findBestTarget();
        if(bestTarget == null) {
            resetCrystalBomber();
            return;
        }
        this.setModuleInfo(bestTarget.getName());
        doCrystalBomber(bestTarget);
    }

    private void doCrystalBomber(EntityPlayer target) {
        if(!timer.hasPassed(delay.getValue().longValue())) {
            return;
        }
        Vec3d playerPos = CombatUtil.interpolateEntity(target);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        BlockPos placePos = blockpos.add(new BlockPos(0, 2, 0));
        int oldSlot = mc.player.inventory.currentItem;
        if(findCrystal() == -1) {
            return;
        }
        if(!CombatUtil.containsCrystal(placePos.add(new BlockPos(0, 1, 0))) && mc.world.getBlockState(placePos).getBlock() == Blocks.OBSIDIAN) {
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                checkTimer.reset();
                if (mc.player.inventory.currentItem != findCrystal()) {
                    mc.player.inventory.currentItem = findCrystal();
                }
                return;
            }
            if (!checkTimer.hasPassed(125L)) {
                return;
            }
            if(rotate.getValue()) {
                EnumFacing placeSide = CombatUtil.getPlaceSide(placePos);
                BlockPos adjacentBlock = placePos.offset(placeSide);
                EnumFacing opposingSide = placeSide.getOpposite();
                Vec3d hitVector = CombatUtil.getHitVector(adjacentBlock, opposingSide);
                final float[] angle = CombatUtil.getLegitRotations(hitVector);
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], angle[1], mc.player.onGround));
            }
            placeCrystal(placePos);
            mc.player.inventory.currentItem = oldSlot;
            placeTimer.reset();
            return;
        }
        if(CombatUtil.containsCrystal(placePos.add(new BlockPos(0, 1, 0))) && mc.world.getBlockState(placePos).getBlock() == Blocks.OBSIDIAN && !hasAlreadySwitched) {
            if (findPickaxe() == -1) {
                return;
            }
            mc.player.inventory.currentItem = findPickaxe();
            hasAlreadySwitched = true;
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, placePos, EnumFacing.UP));
            return;
        }
        if(CombatUtil.containsCrystal(placePos.add(new BlockPos(0, 1, 0))) && mc.world.getBlockState(placePos).getBlock() == Blocks.AIR) {
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                checkTimer.reset();
                if (mc.player.inventory.currentItem != findCrystal()) {
                    mc.player.inventory.currentItem = findCrystal();
                }
                return;
            }
            if (!checkTimer.hasPassed(125L)) {
                return;
            }
            return;
        }
        if(mc.world.getBlockState(placePos).getBlock() == Blocks.AIR) {
            if (findObi() == -1) {
                return;
            }
            if (placeTimer.hasPassed(placeDelay.getValue().longValue())) {
                CombatUtil.placeBlock(placePos, false, rotate.getValue(), false, true, false, findObi());
                justPlacedTimer.reset();
                timer.reset();
                hasAlreadySwitched = false;
            }
        }
    }

    private void placeCrystal(BlockPos pos) {
        EnumFacing facing = EnumFacing.WEST;
        RayTraceResult rayTraceResult;
        rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX + 0.5, mc.player.posY + 1.0, mc.player.posZ + 0.5), new Vec3d(pos));
        // placing that works on 2b :^)
        if (rayTraceResult == null || rayTraceResult.sideHit == null) {
            rayTraceResult = new RayTraceResult(new Vec3d(0.5, 1.0, 0.5), EnumFacing.UP);
            if (rayTraceResult != null) {
                if (rayTraceResult.sideHit != null) {
                    facing = rayTraceResult.sideHit;
                }
            }
        } else {
            facing = rayTraceResult.sideHit;
        }
        EnumHand placeHand = (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, placeHand, (float) rayTraceResult.hitVec.x, (float) rayTraceResult.hitVec.y, (float) rayTraceResult.hitVec.z));
        if(swing.getValue()) {
            mc.player.connection.sendPacket(new CPacketAnimation(placeHand));
        }
    }

    private void findBestTarget() {
        bestTarget = null;
        for (EntityPlayer target : mc.world.playerEntities) {
            if(mc.player.getDistance(target) > enemyRange.getValue()) {
                continue;
            }
            if((target == mc.player || Dactyl.friendManager.isFriend(target.getName()))) {
                continue;
            }
            if (((target).getHealth() <= 0) || target.isDead) {
                continue;
            }
            if(!crystalBomberable(target)) {
                continue;
            }
            if(bestTarget != null) {
                if((mc.player.getDistance(target) < mc.player.getDistance(bestTarget))) {
                    bestTarget = target;
                }
            }
            if(bestTarget == null) {
                bestTarget = target;
            }
        }
    }

    private int findObi() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock)) {
                continue;
            }
            if (((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).getBlock() instanceof BlockObsidian) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private int findPickaxe() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock)) {
                continue;
            }
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemPickaxe) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private int findCrystal() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock)) {
                continue;
            }
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemEndCrystal) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private boolean crystalBomberable(EntityPlayer player) {
        List<BlockPos> adjacentBlocks = Arrays.asList(new BlockPos(0, 2, -1), new BlockPos(-1, 2, 0), new BlockPos(1, 2, 0), new BlockPos(0, 2, 1));
        Vec3d playerPos = CombatUtil.interpolateEntity(player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : adjacentBlocks) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        /*if(!CombatUtil.isCrystalBomberPlaceable(oneBlock.getValue(), blockpos.add(new BlockPos(0, 2, 0)))) {
            return false;
        }*/
        return (size != 0);
    }

    private void resetCrystalBomber() {
        bestTarget = null;
        this.setModuleInfo("");
    }

    private enum TraceMode {
        FULL,
        RANGE,
        OFF
    }

    private enum SwapMode {
        NORMAL,
        SILENT
    }
}
