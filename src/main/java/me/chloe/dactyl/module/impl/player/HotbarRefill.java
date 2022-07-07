package me.chloe.dactyl.module.impl.player;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.CombatUtil;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;

public class HotbarRefill extends Module {
    Setting<Integer> limit = new Setting<Integer>("Limit", 44, 2, 64);
    Setting<Integer> delay = new Setting<Integer>("Delay", 150, 1, 1000);
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
        if (mc.player == null) {
            return;
        }
        if (mc.currentScreen instanceof GuiContainer) {
            return;
        }
        if(!timer.hasPassed(delay.getValue().longValue())) {
            return;
        }
        CombatUtil.Pair<Integer, Integer> slots = CombatUtil.findHotbarRefillSlot(limit.getValue());
        if (slots == null) {
            return;
        }
        final int inventorySlot = slots.getKey();
        final int hotbarSlot = slots.getValue();
        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, hotbarSlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player);
        timer.reset();
    }
}
