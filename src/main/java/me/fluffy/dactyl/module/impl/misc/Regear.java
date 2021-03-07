package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.command.impl.CreateKitCommand;
import me.fluffy.dactyl.injection.inj.access.IGuiShulkerBox;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class Regear extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 100, 1, 1000);
    public Setting<Boolean> onlyAir = new Setting<Boolean>("OnlyAir", true);
    public static Regear INSTANCE;
    public ArrayList<CreateKitCommand.YMLLoadedItem> ymlInventory = new ArrayList<>();
    public Regear() {
        super("Regear", Category.MISC);
        INSTANCE = this;
    }
    private final TimeUtil timer = new TimeUtil();

    @Override
    public void onEnable() {
        if(mc.ingameGUI == null || mc.player == null || mc.world == null) {
            return;
        }
        if(ymlInventory == null || ymlInventory.size() == 0) {
            ChatUtil.printMsg("&c[Regear] Please select a kit using the selectkit command!", true, false);
            this.disable();
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!(mc.currentScreen instanceof GuiShulkerBox)) {
            return;
        }
        if(mc.currentScreen instanceof GuiShulkerBox) {
            GuiShulkerBox guiShulkerBox = (GuiShulkerBox) mc.currentScreen;
            for (int x = 0; x < ((IGuiShulkerBox)guiShulkerBox).getInventory().getSizeInventory(); x++) {
                ItemStack stack = ((IGuiShulkerBox)guiShulkerBox).getInventory().getStackInSlot(x);
                if(stack.isEmpty()) {
                    continue;
                }
                int windowId = mc.player.openContainer.windowId;
                if(ymlInventory == null) return;
                for(CreateKitCommand.YMLLoadedItem loadedItem : ymlInventory) {
                    ItemStack currentStack = ((IGuiShulkerBox)guiShulkerBox).getInventory().getStackInSlot(loadedItem.getSlot()+18);
                    if(!(currentStack.isEmpty() || currentStack.getItem() instanceof ItemAir) && onlyAir.getValue()) return;
                    if(timer.hasPassed(delay.getValue().longValue())) {
                        if (loadedItem.getItemName().equalsIgnoreCase(stack.getItem().getItemStackDisplayName(stack))) {
                            mc.playerController.windowClick(windowId, x, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(windowId, (loadedItem.getSlot() + 18), 0, ClickType.PICKUP, mc.player);
                            //mc.playerController.windowClick(windowId, x, 0, ClickType.PICKUP, mc.player);
                            timer.reset();
                        }
                    }
                }
            }
        }
    }
}
