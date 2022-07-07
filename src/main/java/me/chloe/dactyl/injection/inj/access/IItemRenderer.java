package me.chloe.dactyl.injection.inj.access;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public interface IItemRenderer {
    @Accessor("itemStackMainHand")
    public void setItemStackMain(ItemStack stack);


    @Accessor("itemStackOffHand")
    public void setItemStackOff(ItemStack stack);

    @Accessor("equippedProgressMainHand")
    public void setProgressMain(float equippedProgressMainHand);


    @Accessor("equippedProgressOffHand")
    public void setProgreessOff(float progress);
}
