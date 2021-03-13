package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.HoleUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HoleFill extends Module {
    public Setting<Integer> quota = new Setting<Integer>("Quota", 2, 1, 10);
    public Setting<Integer> delay = new Setting<Integer>("Delay", 45, 1, 400);
    public Setting<Boolean> autoDisable = new Setting<Boolean>("AutoDisable", false);
    public Setting<SwapMode> swapModeSetting = new Setting<SwapMode>("SwapMode", SwapMode.NORMAL);
    public Setting<Boolean> resetSwap = new Setting<Boolean>("ResetSwap", true);
    public Setting<Boolean> offhandOnly = new Setting<Boolean>("OffhandOnly", false);
    public Setting<Boolean> disableSwap = new Setting<Boolean>("DisableSwap", false);
    public Setting<Boolean> smart = new Setting<Boolean>("Smart", false);
    public Setting<Double> playerRange = new Setting<Double>("EntityRange", 6.0d, 1.0d, 10.0d, v->smart.getValue());
    public Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 3.0d, 1.0d, 10.0d, v->smart.getValue());
    public Setting<Double> placeRange = new Setting<Double>("PlaceRange", 3.5d, 0.5d, 6.0d);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }

    private final TimeUtil timer = new TimeUtil();
    int step = 0;
    int recordedSlot = 0;

    @Override
    public void onDisable() {
        timer.reset();
        resetHoleFill();
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
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            resetHoleFill();
            timer.reset();
            return;
        }
        if(swapModeSetting.getValue() == SwapMode.NONE) {
            if(!(mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() instanceof ItemBlock) && !(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
                resetHoleFill();
                return;
            }
            if(!(Block.getBlockFromItem(mc.player.getHeldItemMainhand().getItem()) instanceof BlockObsidian) && !(Block.getBlockFromItem(mc.player.getHeldItemOffhand().getItem()) instanceof BlockObsidian)) {
                resetHoleFill();
                return;
            }
            boolean isOffhanding = (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && Block.getBlockFromItem(mc.player.getHeldItemOffhand().getItem()) instanceof BlockObsidian);
            if(offhandOnly.getValue() && !isOffhanding) {
                resetHoleFill();
                return;
            }
        }
        int size = 0;
        if(getHolesToFill() != null) {
            size = getHolesToFill().size();
        }
        this.setModuleInfo(String.valueOf(size));
        doHollFill();
    }

    private void doHollFill() {
        if(getHolesToFill() == null) {
            return;
        }
        if(getHolesToFill().size() == 0) {
            return;
        }
        if(step >= getHolesToFill().size()) {
            step = 0;
            return;
        }
        if(!timer.hasPassed(delay.getValue().longValue())) {
            return;
        }
        int placed = 0;
        recordedSlot = mc.player.inventory.currentItem;
        for (int i = 0; i < quota.getValue().intValue(); i++) {
            if (step >= getHolesToFill().size()) {
                step = 0;
                break;
            }
            boolean isOffhanding = (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && Block.getBlockFromItem(mc.player.getHeldItemOffhand().getItem()) instanceof BlockObsidian);
            if(!isOffhanding && findObi() == -1) {
                return;
            }
            BlockPos placementPosition = getHolesToFill().get(step);
            boolean blockPlaced = CombatUtil.placeBlock(placementPosition, isOffhanding, rotate.getValue(), false, (swapModeSetting.getValue() != SwapMode.NONE), swapModeSetting.getValue().equals(SwapMode.SILENT), findObi());
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
            if(autoDisable.getValue()) {
                this.toggle();
            }
            timer.reset();
        }
    }

    private List<BlockPos> getHolesToFill() {
        NonNullList positions = NonNullList.create();
        positions.addAll(CombatUtil.getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), placeRange.getValue().floatValue(), placeRange.getValue().intValue(), false, true, 0).stream().filter(pos->isValidHole(pos)).collect(Collectors.toList()));
        return (List<BlockPos>)positions;
    }

    private boolean isValidHole(BlockPos pos) {
        if(mc.world == null) {
            return false;
        }
        List<Entity> playerEntities = new ArrayList<>(mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        EntityPlayer p = null;
        for(Entity ent : playerEntities) {
            if(mc.player.getDistance(ent) >= playerRange.getValue()) {
                continue;
            }
            if(ent.getDistanceSq(pos) > (enemyRange.getValue()*enemyRange.getValue())) {
                continue;
            }
            p = (EntityPlayer) ent;
        }

        if(p == null && smart.getValue()) {
            return false;
        }
        if(!(mc.world.getBlockState(pos).getBlock() instanceof BlockAir)) {
            return false;
        }
        if(!HoleUtil.isHole(pos)) {
            return false;
        }

        return true;
    }


    private void resetHoleFill() {
        this.setModuleInfo("");
        step = 0;
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

    private enum SwapMode {
        NORMAL,
        SILENT,
        NONE
    }
}
