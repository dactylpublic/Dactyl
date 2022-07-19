package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.event.impl.world.SetOpaqueCubeEvent;
import me.chloe.moonlight.module.impl.render.XRay;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class InjVisGraph {
    @Inject(method = "setOpaqueCube", at = @At("HEAD"), cancellable = true)
    public void setOpaqueCube(BlockPos blockPos, CallbackInfo callbackInfo) {
        SetOpaqueCubeEvent setOpaqueCubeEvent = new SetOpaqueCubeEvent();
        MinecraftForge.EVENT_BUS.post(setOpaqueCubeEvent);
        if(setOpaqueCubeEvent.isCanceled()) {
            callbackInfo.cancel();
        }
        try {
            if (XRay.INSTACE.isEnabled()) {
                if(!setOpaqueCubeEvent.isCanceled() && !callbackInfo.isCancelled()) {
                    callbackInfo.cancel();
                }
            }
        }
        catch (Exception ex) {}
    }
}
