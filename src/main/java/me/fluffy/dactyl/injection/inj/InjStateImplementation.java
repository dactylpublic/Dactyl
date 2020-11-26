package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.world.AddedCollisionBoxEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(BlockStateContainer.StateImplementation.class)
public class InjStateImplementation {
    @Shadow
    @Final
    private Block block;

    @Redirect(method = "addCollisionBoxToList", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"))
    public void addCollisionBoxToList(final Block b, final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, @Nullable final Entity entityIn, final boolean isActualState) {
        AddedCollisionBoxEvent event = new AddedCollisionBoxEvent(b, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.block.addCollisionBoxToList(state, worldIn, pos, entityBox, (List)collidingBoxes, entityIn, isActualState);
        }
    }
}
