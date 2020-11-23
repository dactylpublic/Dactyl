package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.PlaceUtil;
import me.fluffy.dactyl.util.RotationUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Scaffold extends Module {
    public Setting<Double> expand = new Setting<Double>("Offset", 1.0d, 0.1d, 6.0d);
    public Setting<Boolean> packetSwitch = new Setting<Boolean>("SilentSwitch", false);

    public static Scaffold INSTANCE;
    public Scaffold() {
        super("Scaffold", Category.PLAYER);
        INSTANCE = this;
    }

    private TimeUtil timerMotion = new TimeUtil();

    private TimeUtil itemTimer = new TimeUtil();

    private BlockData blockData;

    public boolean Switch = true;

    public boolean tower = true;

    public boolean center = true;

    public boolean keepY = false;

    public int lastY;

    public float lastYaw;

    public float lastPitch;

    public BlockPos pos;

    public boolean teleported;


    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if(event.getStage() == ForgeEvent.Stage.PRE) {
            RotationUtil.updateRotations();
            int downDistance = 1;
            if (this.keepY) {
                if ((!((mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F)) && mc.gameSettings.keyBindJump.isKeyDown()) || mc.player.collidedVertically || mc.player.onGround)
                    this.lastY = MathHelper.floor(mc.player.posY);
            } else {
                this.lastY = MathHelper.floor(mc.player.posY);
            }
            this.blockData = null;
            double x = mc.player.posX;
            double z = mc.player.posZ;
            double y = this.keepY ? this.lastY : mc.player.posY;
            double forward = mc.player.movementInput.moveForward;
            double strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if (!mc.player.collidedHorizontally) {
                double[] coords = PlaceUtil.getExpandCoords(x, z, forward, strafe, yaw);
                x = coords[0];
                z = coords[1];
            }
            if (PlaceUtil.canPlace(mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - downDistance, mc.player.posZ)).getBlock())) {
                x = mc.player.posX;
                z = mc.player.posZ;
            }
            BlockPos blockBelow = new BlockPos(x, y - downDistance, z);
            this.pos = blockBelow;
            if (mc.world.getBlockState(blockBelow).getBlock() == Blocks.AIR) {
                this.blockData = getBlockData2(blockBelow);
                if (this.blockData != null) {
                    float yaw1 = PlaceUtil.aimAtLocation(this.blockData.position.getX(), this.blockData.position.getY(), this.blockData.position.getZ(), this.blockData.face)[0];
                    float pitch = PlaceUtil.aimAtLocation(this.blockData.position.getX(), this.blockData.position.getY(), this.blockData.position.getZ(), this.blockData.face)[1];
                    RotationUtil.setPlayerRotations(yaw1, pitch);
                }
            }
        } else if (this.blockData != null) {
            if (PlaceUtil.getBlockCountHotbar() <= 0 || (!this.Switch && mc.player.getHeldItemMainhand().getItem() != null && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock))) {
                return;
            }
            int heldItem = mc.player.inventory.currentItem;
            if (this.Switch && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock))
                for (int j = 0; j < 9; j++) {
                    if (mc.player.inventory.getStackInSlot(j) != null && mc.player.inventory.getStackInSlot(j).getCount() != 0 && mc.player.inventory.getStackInSlot(j).getItem() instanceof ItemBlock && !PlaceUtil.invalid.contains(((ItemBlock)mc.player.inventory.getStackInSlot(j).getItem()).getBlock())) {
                        if(packetSwitch.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(j));
                        } else {
                            mc.player.inventory.currentItem = j;
                        }
                        break;
                    }
                }
            if (this.tower)
                if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F && this.tower && !mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    if (!this.teleported && this.center) {
                        this.teleported = true;
                        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                        mc.player.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    }
                    if (this.center && !this.teleported)
                        return;
                    mc.player.motionY = 0.41999998688697815D;
                    mc.player.motionZ = 0.0D;
                    mc.player.motionX = 0.0D;
                    if (this.timerMotion.sleep(1500L))
                        mc.player.motionY = -0.28D;
                } else {
                    this.timerMotion.reset();
                    if (this.teleported && this.center)
                        this.teleported = false;
                }
            //if (mc.playerController.processRightClickBlock(mc.player, mc.world, this.blockData.position, this.blockData.face, new Vec3d(this.blockData.position.getX() + Math.random(), this.blockData.position.getY() + Math.random(), this.blockData.position.getZ() + Math.random()), EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, this.blockData.position, this.blockData.face, new Vec3d(this.blockData.position.getX() + Math.random(), this.blockData.position.getY() + Math.random(), this.blockData.position.getZ() + Math.random()), EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            //}
            if(packetSwitch.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(heldItem));
            } else {
                mc.player.inventory.currentItem = heldItem;
            }
            RotationUtil.restoreRotations();
        }
    }

    private BlockData getBlockData2(BlockPos pos) {
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(0, -1, 0)).getBlock()))
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(1, 0, 0)).getBlock()))
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(0, 0, 1)).getBlock()))
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(0, 0, -1)).getBlock()))
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos.add(0, 1, 0)).getBlock()))
            return new BlockData(pos.add(0, 1, 0), EnumFacing.DOWN);
        BlockPos pos2 = pos.add(-1, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, -1, 0)).getBlock()))
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 1, 0)).getBlock()))
            return new BlockData(pos2.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, 1)).getBlock()))
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, -1)).getBlock()))
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos3 = pos.add(1, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, -1, 0)).getBlock()))
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 1, 0)).getBlock()))
            return new BlockData(pos3.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, 1)).getBlock()))
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, -1)).getBlock()))
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos4 = pos.add(0, 0, 1);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, -1, 0)).getBlock()))
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 1, 0)).getBlock()))
            return new BlockData(pos4.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, 1)).getBlock()))
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, -1)).getBlock()))
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos5 = pos.add(0, 0, -1);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, -1, 0)).getBlock()))
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 1, 0)).getBlock()))
            return new BlockData(pos5.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, 1)).getBlock()))
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, -1)).getBlock()))
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos6 = pos.add(-2, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, -1, 0)).getBlock()))
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 1, 0)).getBlock()))
            return new BlockData(pos2.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, 1)).getBlock()))
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, -1)).getBlock()))
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos7 = pos.add(2, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, -1, 0)).getBlock()))
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 1, 0)).getBlock()))
            return new BlockData(pos3.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, 1)).getBlock()))
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, -1)).getBlock()))
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos8 = pos.add(0, 0, 2);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, -1, 0)).getBlock()))
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 1, 0)).getBlock()))
            return new BlockData(pos4.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, 1)).getBlock()))
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, -1)).getBlock()))
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos9 = pos.add(0, 0, -2);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, -1, 0)).getBlock()))
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 1, 0)).getBlock()))
            return new BlockData(pos5.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, 1)).getBlock()))
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, -1)).getBlock()))
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos10 = pos.add(0, -1, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(0, -1, 0)).getBlock()))
            return new BlockData(pos10.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(0, 1, 0)).getBlock()))
            return new BlockData(pos10.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos10.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(1, 0, 0)).getBlock()))
            return new BlockData(pos10.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(0, 0, 1)).getBlock()))
            return new BlockData(pos10.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos10.add(0, 0, -1)).getBlock()))
            return new BlockData(pos10.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos11 = pos10.add(1, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(0, -1, 0)).getBlock()))
            return new BlockData(pos11.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(0, 1, 0)).getBlock()))
            return new BlockData(pos11.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos11.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(1, 0, 0)).getBlock()))
            return new BlockData(pos11.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(0, 0, 1)).getBlock()))
            return new BlockData(pos11.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos11.add(0, 0, -1)).getBlock()))
            return new BlockData(pos11.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos12 = pos10.add(-1, 0, 0);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(0, -1, 0)).getBlock()))
            return new BlockData(pos12.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(0, 1, 0)).getBlock()))
            return new BlockData(pos12.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos12.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(1, 0, 0)).getBlock()))
            return new BlockData(pos12.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(0, 0, 1)).getBlock()))
            return new BlockData(pos12.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos12.add(0, 0, -1)).getBlock()))
            return new BlockData(pos12.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos13 = pos10.add(0, 0, 1);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(0, -1, 0)).getBlock()))
            return new BlockData(pos13.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos13.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(0, 1, 0)).getBlock()))
            return new BlockData(pos13.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(1, 0, 0)).getBlock()))
            return new BlockData(pos13.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(0, 0, 1)).getBlock()))
            return new BlockData(pos13.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos13.add(0, 0, -1)).getBlock()))
            return new BlockData(pos13.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos14 = pos10.add(0, 0, -1);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(0, -1, 0)).getBlock()))
            return new BlockData(pos14.add(0, -1, 0), EnumFacing.UP);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(0, 1, 0)).getBlock()))
            return new BlockData(pos14.add(0, 1, 0), EnumFacing.DOWN);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos14.add(-1, 0, 0), EnumFacing.EAST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(1, 0, 0)).getBlock()))
            return new BlockData(pos14.add(1, 0, 0), EnumFacing.WEST);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(0, 0, 1)).getBlock()))
            return new BlockData(pos14.add(0, 0, 1), EnumFacing.NORTH);
        if (!PlaceUtil.invalid.contains(mc.world.getBlockState(pos14.add(0, 0, -1)).getBlock()))
            return new BlockData(pos14.add(0, 0, -1), EnumFacing.SOUTH);
        return null;
    }


    private class BlockData {
        public BlockPos position;

        public EnumFacing face;

        public BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
        }
    }
}
