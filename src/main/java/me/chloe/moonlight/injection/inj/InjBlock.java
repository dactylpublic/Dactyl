package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.module.impl.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class InjBlock{

    
    @Inject(method = { "isFullCube" }, at = { @At("HEAD") }, cancellable = true)
    public void isFullCubeHook(final IBlockState blockState, final CallbackInfoReturnable<Boolean> info) {
        try {
            if (XRay.INSTACE.isEnabled()) {
                info.setReturnValue(XRay.INSTACE.shouldRender(Block.class.cast(this)));
                info.cancel();
            }
        }
        catch (Exception ex) {}
    }
}
