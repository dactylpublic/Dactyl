package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.world.EntityRemovedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class InjWorld {

    @Inject(method = "onEntityRemoved", at = @At("HEAD"), cancellable = true)
    public void onEntityRemoved(Entity entity, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new EntityRemovedEvent(entity));
    }
}
