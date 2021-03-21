package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;

public class FastItem extends Module {
    Setting<Boolean> xpBottles = new Setting<Boolean>("XPBottles", true);
    Setting<Boolean> bows = new Setting<Boolean>("Bows", false);
    Setting<Boolean> autoKickBow = new Setting<Boolean>("AutoKickBow", false, v->!bows.getValue());
    Setting<Integer> delay = new Setting<Integer>("Delay", 3, 1, 10);
    public FastItem() {
        super("FastItem", Category.PLAYER);
    }

    private int tickCount = 0;

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
