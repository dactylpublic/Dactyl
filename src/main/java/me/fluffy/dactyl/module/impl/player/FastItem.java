package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class FastItem extends Module {
    Setting<Boolean> xpBottles = new Setting<Boolean>("XPBottles", true);
    Setting<Boolean> bows = new Setting<Boolean>("Bows", false);
    Setting<Boolean> autoKickBow = new Setting<Boolean>("AutoKickBow", false, v->!bows.getValue());
    Setting<Integer> delay = new Setting<Integer>("Delay", 3, 1, 10);
    Setting<Boolean> ghostFix = new Setting<Boolean>("GhostFix", true);
    public FastItem() {
        super("FastItem", Category.PLAYER);
    }

    private int tickCount = 0;
    private int currentItem = 0;

    @Override
    public void onToggle() {
        if(mc != null && mc.player != null && mc.player.inventory != null) {
            currentItem = mc.player.inventory.currentItem;
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        avoidOverflow();
        tickCount++;
        handleBow();
        handleXPBottle();
        this.setModuleInfo(String.valueOf(delay.getValue()));
    }

    private void handleBow() {
        if (bows.getValue()) {
            if(mc.player.getHeldItemMainhand().getItem().equals(Items.BOW) && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= delay.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                mc.player.stopActiveHand();
            }
            return;
        }
        if(autoKickBow.getValue()) {
            if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 25) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                mc.player.stopActiveHand();
                int shulkerSlot = CombatUtil.findShulkerInHotbar();
                if (shulkerSlot != -1) {
                    CombatUtil.switchToSlot(false, shulkerSlot);
                }
            }
        }
    }
    @SubscribeEvent
    public void onSwitchItem(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketHeldItemChange) {
                CPacketHeldItemChange packet = (CPacketHeldItemChange)event.getPacket();
                currentItem = packet.getSlotId();
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && ghostFix.getValue()) {
                if(xpBottles.getValue()) {
                    ArrayList<Integer> xpbottlesList = new ArrayList<Integer>();
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                        if (stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock)) {
                            if(stack.getItem().equals(Items.EXPERIENCE_BOTTLE)) {
                                xpbottlesList.add(i);
                            }
                        }
                    }
                    if(xpbottlesList.contains(currentItem)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private void handleXPBottle() {
        if(!(tickCount % delay.getValue() == 0.0)) {
            return;
        }
        if(xpBottles.getValue()) {
            if(mc.player.getHeldItemMainhand().getItem().equals(Items.EXPERIENCE_BOTTLE)) {
                mc.rightClickDelayTimer = 0;
            }
        }
    }

    private void avoidOverflow() {
        if(tickCount > 32767) {
            tickCount = 0;
        }
    }
}
