package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.world.ClickBlockEvent;
import me.fluffy.dactyl.event.impl.world.DamageBlockEvent;
import me.fluffy.dactyl.event.impl.world.ResetBlockRemovingEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class InjPlayerControllerMP {

    @Inject(method = {"clickBlock"}, at = {@At("HEAD")})
    private void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        MinecraftForge.EVENT_BUS.post(new ClickBlockEvent(loc, face));
    }

    @Inject(method = {"onPlayerDamageBlock"}, at = {@At("HEAD")})
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        MinecraftForge.EVENT_BUS.post(new DamageBlockEvent(posBlock, directionFacing));
    }

    @Inject(method = {"resetBlockRemoving"}, at = {@At("HEAD")})
    public void onResetBlockRemoving(CallbackInfo ci) {
        ResetBlockRemovingEvent event = new ResetBlockRemovingEvent();
        MinecraftForge.EVENT_BUS.post(event);
    }

    /*@Inject(method = "processRightClickBlock", at=@At("HEAD"), cancellable = true)
    public void onProcessRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> callbackInfo) {
        ProcessRightClickBlockEvent event = new ProcessRightClickBlockEvent();
        if(event.isCanceled()) {
            callbackInfo.setReturnValue(EnumActionResult.FAIL);
            callbackInfo.cancel();
        }
    }*/

}
