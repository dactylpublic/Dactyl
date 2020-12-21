package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SelfTrap extends Module {
    public Setting<Integer> quota = new Setting<Integer>("Quota", 3, 1, 10);
    public Setting<Integer> delay = new Setting<Integer>("Delay", 25, 1, 150);
    public Setting<Boolean> autoDisable = new Setting<Boolean>("AutoDisable", false);
    public Setting<Boolean> autoCenter = new Setting<Boolean>("AutoCenter", true);
    public Setting<SelfTrap.TopMode> topModeSetting = new Setting<SelfTrap.TopMode>("Roof", TopMode.MIN);
    public Setting<SelfTrap.SwapMode> swapModeSetting = new Setting<SelfTrap.SwapMode>("SwapMode", SelfTrap.SwapMode.NORMAL);
    public Setting<Boolean> resetSwap = new Setting<Boolean>("ResetSwap", true);
    public Setting<Boolean> disableSwap = new Setting<Boolean>("DisableSwap", false);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    public SelfTrap() {
        super("SelfTrap", Category.COMBAT);
    }

    private final TimeUtil timer = new TimeUtil();
    int step = 0;
    int recordedSlot = 0;

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(autoCenter.getValue()) {
            CombatUtil.centerToNearestblock();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        timer.reset();
        resetAutoTrap();
        if(disableSwap.getValue()) {
            if(swapModeSetting.getValue() == SelfTrap.SwapMode.NORMAL) {
                mc.player.inventory.currentItem = recordedSlot;
            } else if(swapModeSetting.getValue() == SelfTrap.SwapMode.SILENT) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(recordedSlot));
            }
            recordedSlot = 0;
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            resetAutoTrap();
            timer.reset();
            return;
        }
        if(swapModeSetting.getValue() == SelfTrap.SwapMode.NONE) {
            if(!(mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
                resetAutoTrap();
                return;
            }
            if(!(Block.getBlockFromItem(mc.player.getHeldItemMainhand().getItem()) instanceof BlockObsidian) && !(Block.getBlockFromItem(mc.player.getHeldItemOffhand().getItem()) instanceof BlockObsidian)) {
                resetAutoTrap();
                return;
            }
        }
        this.setModuleInfo(topModeSetting.getValue().toString());
        doSelfTrap();
    }

    private void doSelfTrap() {
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
            BlockPos placementPosition = new BlockPos(mc.player.getPositionVector()).down().add(getTrap().get(step).x, getTrap().get(step).y, getTrap().get(step).z);
            boolean blockPlaced = CombatUtil.placeBlock(placementPosition, false, rotate.getValue(), false, (swapModeSetting.getValue() != SwapMode.NONE), swapModeSetting.getValue().equals(SelfTrap.SwapMode.SILENT), findObi());
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
        switch((SelfTrap.SwapMode)swapModeSetting.getValue()) {
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

    private List<Vec3d> getTrap() {
        List<Vec3d> vec3ds = new ArrayList<>();
        switch((SelfTrap.TopMode)topModeSetting.getValue()) {
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
        step = 0;
    }


    public enum TopMode {
        TOOBEE("2b2t"),
        FULL("Full"),
        DOUBLE("Double"),
        MIN("Minimum");

        private final String name;

        private TopMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private enum SwapMode {
        NORMAL,
        SILENT,
        NONE
    }
}
