package me.chloe.moonlight.injection.inj.access;

import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiShulkerBox.class)
public interface IGuiShulkerBox {
    @Accessor("inventory")
    public IInventory getInventory();

    @Accessor("playerInventory")
    public InventoryPlayer getPlayerInventory();
}
