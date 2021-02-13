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
    public FastItem() {
        super("FastItem", Category.PLAYER);
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        handleBow();
        handleXPBottle();
    }

    private void handleBow() {
        if (bows.getValue()) {
            if(mc.player.getHeldItemMainhand().getItem().equals(Items.BOW) && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3) {
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
        if(xpBottles.getValue()) {
            if(mc.player.getHeldItemMainhand().getItem().equals(Items.EXPERIENCE_BOTTLE)) {
                mc.rightClickDelayTimer = 0;
            }
        }
    }
}
