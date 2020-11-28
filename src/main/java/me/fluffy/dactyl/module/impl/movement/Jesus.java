package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.PlayerMotionEvent;
import me.fluffy.dactyl.event.impl.world.AddedCollisionBoxEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.player.Freecam;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Jesus extends Module {

    public static Jesus INSTANCE;
    public Jesus() {
        super("Jesus", Category.MOVEMENT);
        INSTANCE = this;
    }

    private static final AxisAlignedBB WATER_WALK_AA = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0);

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if (!Freecam.INSTANCE.isEnabled() && EntityUtil.isInWater((Entity) Jesus.mc.player) && !Jesus.mc.player.isSneaking()) {
            Jesus.mc.player.motionY = 0.1;
            if (Jesus.mc.player.getRidingEntity() != null && !(Jesus.mc.player.getRidingEntity() instanceof EntityBoat)) {
                Jesus.mc.player.getRidingEntity().motionY = 0.3;
            }
        }
        this.setModuleInfo("Flat");
    }

    @SubscribeEvent
    public void onCollision(AddedCollisionBoxEvent event) {
        if (Jesus.mc.player != null && event.getBlock() instanceof BlockLiquid && ((event.getEntity() != null && event.getEntity().equals(mc.player.getRidingEntity())) || event.getEntity() == Jesus.mc.player) && !(event.getEntity() instanceof EntityBoat) && !Jesus.mc.player.isSneaking() && Jesus.mc.player.fallDistance < 3.0f && !EntityUtil.isInWater((Entity) Jesus.mc.player) && (EntityUtil.isAboveWater((Entity) Jesus.mc.player, false) || EntityUtil.isAboveWater(Jesus.mc.player.getRidingEntity(), false)) && isAboveBlock((Entity) Jesus.mc.player, event.getPos())) {
            final AxisAlignedBB axisalignedbb = Jesus.WATER_WALK_AA.offset(event.getPos());
            if (event.getEntityBox().intersects(axisalignedbb)) {
                event.getCollidingBoxes().add(axisalignedbb);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof CPacketPlayer && EntityUtil.isAboveWater((Entity) Jesus.mc.player, true) && !EntityUtil.isInWater((Entity) Jesus.mc.player) && !isAboveLand((Entity) Jesus.mc.player)) {
            final int ticks = Jesus.mc.player.ticksExisted % 2;
            if (ticks == 0) {
                final CPacketPlayer cPacketPlayer = (CPacketPlayer) event.getPacket();
                cPacketPlayer.y += 0.02;
            }
        }
    }

    private static boolean isAboveLand(final Entity entity) {
        if (entity == null) {
            return false;
        }
        final double y = entity.posY - 0.01;
        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); ++x) {
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); ++z) {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock().isFullBlock(mc.world.getBlockState(pos))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAboveBlock(final Entity entity, final BlockPos pos) {
        return entity.posY >= pos.getY();
    }
}
