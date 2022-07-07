package me.chloe.dactyl.injection.inj;

import me.chloe.dactyl.event.impl.action.BoatMoveEvent;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityBoat.class)
public class InjEntityBoat {
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityBoat;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void onMove(EntityBoat entityBoat, MoverType type, double x, double y, double z) {
        BoatMoveEvent boatMoveEvent = new BoatMoveEvent(entityBoat, x, y, z);
        MinecraftForge.EVENT_BUS.post(boatMoveEvent);
        if(!boatMoveEvent.isCanceled()) entityBoat.move(type, boatMoveEvent.x, boatMoveEvent.y, boatMoveEvent.z);
    }
}
