package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.world.EntityAddedEvent;
import me.fluffy.dactyl.event.impl.world.EntityRemovedEvent;
import me.fluffy.dactyl.module.impl.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class InjWorld {

    @Inject(method = "onEntityRemoved", at = @At("HEAD"), cancellable = true)
    public void onEntityRemoved(Entity entity, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new EntityRemovedEvent(entity));
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"), cancellable = true)
    public void onEntityAdded(Entity entity, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new EntityAddedEvent(entity));
    }

    @Inject(method = {"checkLightFor"}, at = {@At("HEAD")}, cancellable = true)
    private void checkLightFor(EnumSkyBlock enumSkyBlock, BlockPos blockPos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.noSkylight.getValue()) {
            callbackInfoReturnable.setReturnValue(Boolean.valueOf(false));
        }
    }
}
