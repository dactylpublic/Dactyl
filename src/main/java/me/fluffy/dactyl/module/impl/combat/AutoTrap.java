package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoTrap extends Module {
    public Setting<Integer> quota = new Setting<Integer>("Quota", 3, 1, 10);
    public Setting<Integer> delay = new Setting<Integer>("Delay", 25, 1, 150);
    public Setting<TraceMode> raytrace = new Setting<TraceMode>("RayTrace", TraceMode.OFF);
    public Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 6.0D, 1.0D, 10.0D);
    public Setting<Double> placeRange = new Setting<Double>("PlaceRange", 4.7D, 1.0D, 6.0D);
    public Setting<Double> wallsPlace = new Setting<Double>("WallsRange", 4.0D, 1.0D, 6.0D, vis->raytrace.getValue() == TraceMode.RANGE);
    public Setting<Boolean> autoDisable = new Setting<Boolean>("AutoDisable", false);
    public Setting<TopMode> topModeSetting = new Setting<TopMode>("Roof", TopMode.DOUBLE);
    public Setting<SwapMode> swapModeSetting = new Setting<SwapMode>("SwapMode", SwapMode.NORMAL);
    public Setting<Boolean> resetSwap = new Setting<Boolean>("ResetSwap", true);
    public Setting<Boolean> disableSwap = new Setting<Boolean>("DisableSwap", false);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);


    public AutoTrap() {
        super("AutoTrap", Category.COMBAT);
    }

    private final TimeUtil timer = new TimeUtil();
    int step = 0;
    int recordedSlot = 0;
    private EntityPlayer bestTarget;

    @Override
    public void onDisable() {
        super.onDisable();
        if(disableSwap.getValue()) {
            if(swapModeSetting.getValue() == SwapMode.NORMAL) {
                mc.player.inventory.currentItem = recordedSlot;
            } else if(swapModeSetting.getValue() == SwapMode.SILENT) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(recordedSlot));
            }
            recordedSlot = 0;
        }
    }

    @Override
    public void onToggle() {
        bestTarget = null;
        step = 0;
        timer.reset();
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            resetAutoTrap();
            timer.reset();
            return;
        }
        findBestTarget();
        if(bestTarget == null) {
            resetAutoTrap();
            return;
        }
        if(swapModeSetting.getValue() == SwapMode.NONE) {
            if(!(mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() instanceof ItemBlock)) {
                resetAutoTrap();
                this.setModuleInfo("");
                return;
            }
            if (!(((ItemBlock) mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem()).getBlock() instanceof BlockObsidian)) {
                resetAutoTrap();
                this.setModuleInfo("");
                return;
            }
        }
        this.setModuleInfo(bestTarget.getName());
        doAutoTrap(bestTarget);
    }

    /*@SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {

    }*/

    private void doAutoTrap(EntityPlayer target) {
        if(step >= getTrap().size()) {
            step = 0;
            if(autoDisable.getValue()) {
                this.toggle();
            }
            return;
        }
        if(!timer.hasPassed(delay.getValue().longValue())) {
            return;
        }
        int placed = 0;
        recordedSlot = mc.player.inventory.currentItem;
        for (int i = 0; i < quota.getValue().intValue(); i++) {
            if (step >= getTrap().size()) {
                step = 0;
                if (autoDisable.getValue()) {
                    this.toggle();
                }
                break;
            }
            if(findObi() == -1) {
                return;
            }
            BlockPos placementPosition = new BlockPos(target.getPositionVector()).down().add(getTrap().get(step).x, getTrap().get(step).y, getTrap().get(step).z);
            boolean canSeeBlock = CombatUtil.canSeeBlock(placementPosition);
            double rangeBasedSettings = placeRange.getValue().doubleValue();
            if(!canSeeBlock && raytrace.getValue() == TraceMode.RANGE) {
                rangeBasedSettings = wallsPlace.getValue().doubleValue();
            }
            if(raytrace.getValue() == TraceMode.FULL) {
                if(!canSeeBlock) {
                    step++;
                    return;
                }
            }
            double distToBlock = Math.sqrt((mc.player.posX - placementPosition.getX() * mc.player.posX - placementPosition.getX()) + (mc.player.posY - placementPosition.getY() * mc.player.posY - placementPosition.getY() + (mc.player.posZ - placementPosition.getZ() * mc.player.posZ - placementPosition.getZ())));
            if(distToBlock > rangeBasedSettings) {
                step++;
                return;
            }
            boolean blockPlaced = CombatUtil.placeBlock(placementPosition, false, rotate.getValue(), false, true, swapModeSetting.getValue().equals(SwapMode.SILENT), findObi());
            if (blockPlaced) {
                placed++;
            }
            step++;
        }

        if(placed > 0) {
            if(resetSwap.getValue()) {
                doResetSwap(recordedSlot);
                recordedSlot = mc.player.inventory.currentItem;

            }
            timer.reset();
        }
    }

    private void doResetSwap(int recordedSlot) {
        switch((SwapMode)swapModeSetting.getValue()) {
            case NORMAL:
                if(mc.player.inventory.currentItem != recordedSlot) {
                    mc.player.inventory.currentItem = recordedSlot;
                }
                break;
            case SILENT:
                mc.player.connection.sendPacket(new CPacketHeldItemChange(recordedSlot));
                break;
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


    private void findBestTarget() {
        bestTarget = null;
        for (EntityPlayer target : mc.world.playerEntities) {
            if((target == mc.player || Dactyl.friendManager.isFriend(target.getName()))) {
                continue;
            }
            if (((target).getHealth() <= 0) || target.isDead) {
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
    }

    private List<Vec3d> getTrap() {
        List<Vec3d> vec3ds = new ArrayList<>();
        switch((TopMode)topModeSetting.getValue()) {
            case FULL:
                addBaseTrap(vec3ds);
                vec3ds.add(new Vec3d(0, 3, -1));
                vec3ds.add(new Vec3d(1, 3, 0));
                vec3ds.add(new Vec3d(0, 3, 1));
                vec3ds.add(new Vec3d(-1, 3, 0));
                vec3ds.add(new Vec3d(0, 3, 0));
                break;
            case DOUBLE:
                addBaseTrap(vec3ds);
                vec3ds.add(new Vec3d(0, 3, -1));
                vec3ds.add(new Vec3d(0, 3, 0));
                vec3ds.add(new Vec3d(0, 4, 0));
                break;
            case MIN:
                addBaseTrap(vec3ds);
                vec3ds.add(new Vec3d(0, 3, -1));
                vec3ds.add(new Vec3d(0, 3, 0));
                break;
            default:
                vec3ds.add(new Vec3d(0, 0, -1));
                vec3ds.add(new Vec3d(1, 0, 0));
                vec3ds.add(new Vec3d(0, 0, 1));
                vec3ds.add(new Vec3d(-1, 0, 0));
                vec3ds.add(new Vec3d(0, 1, -1));
                vec3ds.add(new Vec3d(1, 1, 0));
                vec3ds.add(new Vec3d(0, 1, 1));
                vec3ds.add(new Vec3d(-1, 1, 0));
                switch((EnumFacing)mc.player.getHorizontalFacing()) {
                    // -Z
                    case NORTH:
                        vec3ds.add(new Vec3d(0, 2, -1));
                        vec3ds.add(new Vec3d(0, 3, -1));
                        vec3ds.add(new Vec3d(0, 3, 0));
                        vec3ds.add(new Vec3d(1, 2, 0));
                        vec3ds.add(new Vec3d(0, 2, 1));
                        vec3ds.add(new Vec3d(-1, 2, 0));
                        break;
                    // +X
                    case EAST:
                        vec3ds.add(new Vec3d(1, 2, 0));
                        vec3ds.add(new Vec3d(1, 3, 0));
                        vec3ds.add(new Vec3d(0, 3, 0));
                        vec3ds.add(new Vec3d(0, 2, -1));
                        vec3ds.add(new Vec3d(0, 2, 1));
                        vec3ds.add(new Vec3d(-1, 2, 0));
                        break;
                    // +Z
                    case SOUTH:
                        vec3ds.add(new Vec3d(0, 2, 1));
                        vec3ds.add(new Vec3d(0, 3, 1));
                        vec3ds.add(new Vec3d(0, 3, 0));
                        vec3ds.add(new Vec3d(0, 2, -1));
                        vec3ds.add(new Vec3d(1, 2, 0));
                        vec3ds.add(new Vec3d(-1, 2, 0));
                        break;
                    // WEST: -X
                    default:
                        vec3ds.add(new Vec3d(-1, 2, 0));
                        vec3ds.add(new Vec3d(-1, 3, 0));
                        vec3ds.add(new Vec3d(0, 3, 0));
                        vec3ds.add(new Vec3d(0, 2, -1));
                        vec3ds.add(new Vec3d(1, 2, 0));
                        vec3ds.add(new Vec3d(0, 2, 1));
                        break;
                }
                break;
        }
        return vec3ds;
    }

    private void addBaseTrap(List<Vec3d> vec3ds) {
        vec3ds.add(new Vec3d(0, 0, -1));
        vec3ds.add(new Vec3d(1, 0, 0));
        vec3ds.add(new Vec3d(0, 0, 1));
        vec3ds.add(new Vec3d(-1, 0, 0));
        vec3ds.add(new Vec3d(0, 1, -1));
        vec3ds.add(new Vec3d(1, 1, 0));
        vec3ds.add(new Vec3d(0, 1, 1));
        vec3ds.add(new Vec3d(-1, 1, 0));
        vec3ds.add(new Vec3d(0, 2, -1));
        vec3ds.add(new Vec3d(1, 2, 0));
        vec3ds.add(new Vec3d(0, 2, 1));
        vec3ds.add(new Vec3d(-1, 2, 0));
    }

    private void resetAutoTrap() {
        bestTarget = null;
        this.setModuleInfo("");
        step = 0;
    }



    private enum TraceMode {
        FULL,
        RANGE,
        OFF
    }

    private enum TopMode {
        TOOBEE,
        FULL,
        DOUBLE,
        MIN
    }

    private enum SwapMode {
        NORMAL,
        SILENT,
        NONE
    }
}
