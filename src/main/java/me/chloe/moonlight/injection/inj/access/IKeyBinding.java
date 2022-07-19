package me.chloe.moonlight.injection.inj.access;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface IKeyBinding {
    @Accessor("pressed")
    public void setPressed(boolean pressed);
}
