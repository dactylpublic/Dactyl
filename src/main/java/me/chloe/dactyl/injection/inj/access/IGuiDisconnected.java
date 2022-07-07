package me.chloe.dactyl.injection.inj.access;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface IGuiDisconnected {
    @Accessor("reason")
    public String getReason();

    @Accessor("message")
    public ITextComponent getMessage();
}
