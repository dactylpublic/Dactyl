package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.misc.MiningTweaks;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoHoleMine extends Module {
    public Setting<Boolean> switchToPick = new Setting<Boolean>("PickaxeSwitch", true);
    public Setting<Double> breakRange = new Setting<Double>("BreakRange", 3.5d, 1.0d, 6.0d);
    public Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 6.0d, 1.0d, 10.0d);
    public Setting<Boolean> oneBlock = new Setting<Boolean>("1.13+", false);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    public Setting<Boolean> autoDisable = new Setting<Boolean>("AutoDisable", true);
    public AutoHoleMine() {
        super("AutoHoleMine", Category.COMBAT, "Only usable with PacketMine");
    }

    private boolean tappedBlock = false;
    private boolean switchedToPickaxe = false;

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if(mc.player == null || mc.world == null) {
            return;
        }

        if(!MiningTweaks.INSTANCE.isEnabled()) {
            this.toggleWithReason("&b[AutoHoleMine] &cBreakTweaks is not enabled!");
            return;
        } else {
            if(MiningTweaks.INSTANCE.modeSetting.getValue() != MiningTweaks.MiningMode.BYPASS && MiningTweaks.INSTANCE.modeSetting.getValue() != MiningTweaks.MiningMode.PACKET) {
                this.toggleWithReason("&b[AutoHoleMine] &cBreakTweaks mode is not Packet or Bypass!");
                return;
            }
        }
        EntityPlayer target = findClosestTarget();
        this.setModuleInfo(target == null ? "" : target.getName());
        if(target != null && getCity(target) != null) {
            BlockPos[] cityOffsets = getCity(target);
            Vec3d playerPos = CombatUtil.interpolateEntity(target);
            BlockPos interpolatedPos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
            if((!tappedBlock || MiningTweaks.INSTANCE.currentPos == null) && cityOffsets != null) {
                mc.playerController.onPlayerDamageBlock(interpolatedPos.add(cityOffsets[0]), EnumFacing.UP);
                tappedBlock = true;
            }
            if(rotate.getValue() && cityOffsets != null && !switchedToPickaxe) {
                EnumFacing placeSide = CombatUtil.getPlaceSide(interpolatedPos.add(cityOffsets[0]));
                BlockPos adjacentBlock = interpolatedPos.add(cityOffsets[0]).offset(placeSide);
                EnumFacing opposingSide = placeSide.getOpposite();
                Vec3d hitVector = CombatUtil.getHitVector(adjacentBlock, opposingSide);
                final float[] angle = CombatUtil.getLegitRotations(hitVector);
                event.setYaw(angle[0]);
                mc.player.rotationYawHead = angle[0];
                event.setPitch(angle[1]);
            }
            if(!switchedToPickaxe && tappedBlock && MiningTweaks.INSTANCE.isBlockDoneMining ) {
                if(switchToPick.getValue()) {
                    if (mc.player.inventory.currentItem != findPickaxe() && findPickaxe() != -1) {
                        mc.player.inventory.currentItem = findPickaxe();
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(findPickaxe()));
                    }
                }
                switchedToPickaxe = true;
            }
        } else {
            reset();
        }
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

    private void reset() {
        tappedBlock = false;
        switchedToPickaxe = false;
    }

    private EntityPlayer findClosestTarget() {
        EntityPlayer bestTarget = null;
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
            if(!isCityable(target)) {
                continue;
            }
            if(bestTarget != null) {
                if((mc.player.getDistance(target) < mc.player.getDistance(bestTarget)) && (mc.player.getDistance(target) < enemyRange.getValue().doubleValue())) {
                    bestTarget = target;
                }
            }
            if(bestTarget == null && (mc.player.getDistance(target) < enemyRange.getValue().doubleValue())) {
                bestTarget = target;
            }
        }
        return bestTarget;
    }

    private BlockPos[] northCity = {
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, -2),
            new BlockPos(0, -1, -2),
            new BlockPos(0, 0, -2)
    };

    private BlockPos[] eastCity = {
            new BlockPos(1, 0, 0),
            new BlockPos(2, 0, 0),
            new BlockPos(2, -1, 0),
            new BlockPos(2, 0, 0)
    };

    private BlockPos[] southCity = {
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, 2),
            new BlockPos(0, -1, 2),
            new BlockPos(0, 0, 2)
    };

    private BlockPos[] westCity = {
            new BlockPos(-1, 0, 0),
            new BlockPos(-2, 0, 0),
            new BlockPos(-2, -1, 0),
            new BlockPos(-2, 0, 0)
    };

    private BlockPos[] getCity(EntityPlayer player) {
        Vec3d playerPos = CombatUtil.interpolateEntity(player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int sizeNorth = 0;
        for(BlockPos bPos : northCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeNorth++;
            }
        }

        int sizeEast = 0;
        for(BlockPos bPos : eastCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeEast++;
            }
        }

        int sizeSouth = 0;
        for(BlockPos bPos : southCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeSouth++;
            }
        }

        int sizeWest = 0;
        for(BlockPos bPos : westCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeWest++;
            }
        }
        if(sizeNorth == 4) {
            return northCity;
        }
        if(sizeEast == 4) {
            return eastCity;
        }
        if(sizeSouth == 4) {
            return southCity;
        }
        if(sizeWest == 4) {
            return westCity;
        }
        return null;
    }

    private boolean isCityable(EntityPlayer player) {
        Vec3d playerPos = CombatUtil.interpolateEntity(player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int sizeNorth = 0;
        for(BlockPos bPos : northCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeNorth++;
            }
        }

        int sizeEast = 0;
        for(BlockPos bPos : eastCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeEast++;
            }
        }

        int sizeSouth = 0;
        for(BlockPos bPos : southCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeSouth++;
            }
        }

        int sizeWest = 0;
        for(BlockPos bPos : westCity) {
            if(canAddSize(blockpos.add(bPos), bPos) && mc.player.getDistance(blockpos.add(bPos).getX(), blockpos.add(bPos).getY(), blockpos.add(bPos).getZ()) <= breakRange.getValue()) {
                sizeWest++;
            }
        }
        return (sizeNorth == 4 || sizeEast == 4 || sizeSouth == 4 || sizeWest == 4);
    }

    private boolean canAddSize(BlockPos pos, BlockPos unaddedOrigin) {
        if(unaddedOrigin.getY() == -1) {
            if(CombatUtil.isValidPlacePos(oneBlock.getValue(), pos)) {
                return true;
            }
        } else {
            if(unaddedOrigin.getX() == 1 || unaddedOrigin.getX() == -1 || unaddedOrigin.getZ() == 1 || unaddedOrigin.getZ() == -1) {
                if (CombatUtil.isHard(mc.world.getBlockState(pos).getBlock())) {
                    return true;
                }
            } else {
                if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR) {
                    return true;
                }
            }
        }
        return false;
    }

    private class CityBlock extends BlockPos{

        public Block block;
        public boolean isHard;

        public CityBlock(double x, double y, double z) {
            super(x, y, z);
        }

        public CityBlock(double x, double y, double z, Block block) {
            super(x, y, z);
            this.block = block;
            this.isHard = false;
        }

        public CityBlock(double x, double y, double z, Block block, boolean isHard) {
            super(x, y, z);
            this.block = block;
            this.isHard = isHard;
        }

        public BlockPos getBlockPos() {
            return new BlockPos(this.getX(), this.getY(), this.getZ());
        }
    }
}
