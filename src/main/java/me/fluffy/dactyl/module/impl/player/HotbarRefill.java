package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HotbarRefill extends Module {
    Setting<Integer> limit = new Setting<Integer>("Limit", 44, 2, 64);
    Setting<Integer> delay = new Setting<Integer>("Delay", 50, 1, 500);
    public HotbarRefill() {
        super("HotbarRefill", Category.PLAYER);
    }

    private TimeUtil timer = new TimeUtil();

    @Override
    public void onToggle() {
        timer.reset();
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null || mc.currentScreen != null || !timer.hasPassed(delay.getValue().longValue())) {
            return;
        }
        for (int x = 0; x < 9; x++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(x);
            if (stack.isEmpty() || stack.getItem() == Items.AIR || !stack.isStackable() || stack.getCount() >= stack.getMaxStackSize() || stack.getCount() > limit.getValue()) {
                return;
            }
            int foundItemSlot = CombatUtil.findItemSlotNotHotbar(stack.getItem());
            if(foundItemSlot != -1) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, foundItemSlot, 0, ClickType.QUICK_MOVE, mc.player);
                mc.playerController.updateController();
                timer.reset();
            }
        }
    }

}
