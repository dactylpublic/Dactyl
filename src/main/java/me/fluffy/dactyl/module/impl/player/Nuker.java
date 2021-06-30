package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Nuker extends Module {
    public Setting<NukerMode> modeSetting = new Setting<NukerMode>("Mode", NukerMode.SURVIVAL);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    public Setting<Boolean> flatten = new Setting<Boolean>("Flatten", true);
    public Setting<Boolean> multipointRotate = new Setting<Boolean>("MPRotate", true, v->rotate.getValue());
    public Setting<Boolean> blackList = new Setting<Boolean>("Blacklist", false, "Makes selective mode a blacklist for blocks instead of a whitelist for blocks.");
    public Setting<Double> range = new Setting<Double>("Range", 4.5d, 0.1d, 6.0d);
    public Nuker() {
        super("Nuker", Category.PLAYER);
    }

    private BlockPos focus;
    private double yaw;
    private double pitch;
    private boolean isRotating;

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if(event.getStage() == ForgeEvent.Stage.PRE) {
            return;
        }
        for (double y = mc.player.posY + range.getValue(); y > mc.player.posY - (flatten.getValue() ? 0.5 : range.getValue()); y--) {
            for (double x = mc.player.posX - range.getValue(); x < mc.player.posX + range.getValue(); x++) {
                for (double z = mc.player.posZ - range.getValue(); z < mc.player.posZ + range.getValue(); z++) {
                    BlockPos position = new BlockPos(x, y, z); // Target a specific block
                    if (isBlockValid(position)) {
                        if (focus == null) {
                            focus = position; // Set the first block found if the current target is null
                        }
                        else if (mc.player.getDistance(focus.getX(), focus.getY(), focus.getZ()) > mc.player.getDistance(x, y, z)) {
                            focus = position; // Check to see if there is a closer block that can be targeted
                        }
                    }
                }
            }
        }

        // Make sure there is a potential target and the held item is plantable
        if (focus == null) {
            isRotating = false;
            return;
        } else {
            isRotating = true;
        }

        // Aim at the block
        double[] rotations = CombatUtil.calculateLookAt(focus.getX() + 0.5, focus.getY() - 0.5, focus.getZ() + 0.5);
        if(multipointRotate.getValue()) {
            rotations = CombatUtil.calculateLookAtBlock(focus);
        }
        yaw = rotations[0];
        pitch = rotations[1];
        if (focus != null) {
            if (isBlockValid(focus)) {
                if(mc.playerController.currentGameType == GameType.CREATIVE) {
                    if (mc.playerController.clickBlock(focus, getFacingDirectionToPosition(focus))) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                } else if(mc.playerController.currentGameType == GameType.SURVIVAL) {
                    if (BlockManager.GetCurrBlock() == null || (BlockManager.GetCurrBlock().getX() != focus.getX() && BlockManager.GetCurrBlock().getY() != focus.getY() && BlockManager.GetCurrBlock().getZ() != focus.getZ())) {
                        BlockManager.SetCurrentBlock(focus);
                    }
                    if(BlockManager.GetCurrBlock() != null) {
                        BlockManager.Update(range.getValue().floatValue(), false);
                    }
                }
            } else {
                focus = null;
            }
        }
    }



    @SubscribeEvent
    public void onUpdateRotate(EventUpdateWalkingPlayer event) {
        if(rotate.getValue()) {
            if (event.getStage() == ForgeEvent.Stage.PRE) {
                if (isRotating) {
                    event.setYaw((float) yaw);
                    event.setPitch((float) pitch);
                }
            }
        }
    }

    private float[] getRotations(BlockPos position, EnumFacing facing)
    {
        double xDifference = (position.getX() + 0.5D + facing.getDirectionVec().getX() * 0.25D) - mc.player.posX;
        double yDifference = (position.getY() + 0.5D + facing.getDirectionVec().getY() * 0.25D) - mc.player.posY;
        double zDifference = (position.getZ() + 0.5D + facing.getDirectionVec().getZ() * 0.25D) - mc.player.posZ;
        double positions = MathHelper.sqrt(xDifference * xDifference + zDifference * zDifference);
        float yaw = (float)(Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) - (Math.atan2(yDifference, positions) * 180.0D / Math.PI);
        return new float[] {yaw, pitch};
    }

    private boolean isBlockValid(BlockPos position)
    {
        boolean valid = false;
        Block target = mc.world.getBlockState(position).getBlock();

        if(modeSetting.getValue() == NukerMode.SELECTIVE) {
            if (mc.playerController.currentGameType == GameType.SURVIVAL && (target == Blocks.BEDROCK || target == Blocks.AIR || target == Blocks.WATER || target == Blocks.LAVA || target == Blocks.FLOWING_WATER || target == Blocks.FLOWING_LAVA || target instanceof BlockLiquid)) {
                return false;
            }
            if(blackList.getValue()) {
                if (!Dactyl.nukerManager.hasBlock(target)) {
                    valid = true;
                }
            } else {
                if (Dactyl.nukerManager.hasBlock(target)) {
                    valid = true;
                }
            }
        }

        if(modeSetting.getValue() == NukerMode.CROPS) {
            if (target instanceof BlockCrops) {
                BlockCrops crops = (BlockCrops) target;
                valid = crops.getMetaFromState(mc.world.getBlockState(position)) == 7;
            } else if (target instanceof BlockNetherWart) {
                BlockNetherWart wart = (BlockNetherWart) target;
                valid = wart.getMetaFromState(mc.world.getBlockState(position)) == 3; // Ensures the wart is fully grown
            } else if (target instanceof BlockReed) {
                valid = mc.player.world.getBlockState(position.down()).getBlock() instanceof BlockReed; // Ensures the reed is above another reed, meaning it has grown
            }
        }

        if (modeSetting.getValue() == NukerMode.CREATIVE || modeSetting.getValue() == NukerMode.SURVIVAL) {
            if (mc.playerController.currentGameType == GameType.SURVIVAL && (target == Blocks.BEDROCK || target == Blocks.AIR || target == Blocks.WATER || target == Blocks.LAVA || target == Blocks.FLOWING_WATER || target == Blocks.FLOWING_LAVA || target instanceof BlockLiquid)) {
                return false;
            }
            if (target instanceof Block && !(target instanceof BlockAir)) {
                valid = true;
            }
        }


        return valid && this.getFacingDirectionToPosition(position) != null && mc.player.getDistance(position.getX(), position.getY(), position.getZ()) < mc.playerController.getBlockReachDistance() - 1.0D;
    }

    private EnumFacing getFacingDirectionToPosition(BlockPos position)
    {
        EnumFacing direction = null;

        if (!isSolidFullCube(mc.world.getBlockState(position.add(0, 1, 0))))
        {
            direction = EnumFacing.UP;
        }
        else if (!isSolidFullCube(mc.world.getBlockState(position.add(0, -1, 0))))
        {
            direction = EnumFacing.DOWN;
        }
        else if (!isSolidFullCube(mc.world.getBlockState(position.add(1, 0, 0))))
        {
            direction = EnumFacing.EAST;
        }
        else if (!isSolidFullCube(mc.world.getBlockState(position.add(-1, 0, 0))))
        {
            direction = EnumFacing.WEST;
        }
        else if (!isSolidFullCube(mc.world.getBlockState(position.add(0, 0, 1))))
        {
            direction = EnumFacing.SOUTH;
        }
        else if (!isSolidFullCube(mc.world.getBlockState(position.add(0, 0, 1))))
        {
            direction = EnumFacing.NORTH;
        }

        return direction;
    }

    public boolean isSolidFullCube(IBlockState blockState)
    {
        return blockState.getMaterial().blocksMovement();
    }


    private enum NukerMode {
        CREATIVE("Creative"),
        SURVIVAL("Survival"),
        CROPS("Crops"),
        SELECTIVE("Selective");

        private final String name;

        private NukerMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }


    }

    /*
    fuck you its only nuker who cares about copied code its fucking nuker
     */

    private static class BlockManager
    {
        private static Minecraft mc = Minecraft.getMinecraft();

        private static BlockPos _currBlock = null;
        private static boolean _started = false;

        public static void SetCurrentBlock(BlockPos block)
        {
            _currBlock = block;
            _started = false;
        }

        public static BlockPos GetCurrBlock()
        {
            return _currBlock;
        }

        public static boolean GetState()
        {
            if (_currBlock != null)
                return IsDoneBreaking(mc.world.getBlockState(_currBlock));

            return false;
        }

        private static boolean IsDoneBreaking(IBlockState blockState)
        {
            return blockState.getBlock() == Blocks.BEDROCK
                    || blockState.getBlock() == Blocks.AIR
                    || blockState.getBlock() instanceof BlockLiquid;
        }

        public static boolean Update(float range, boolean rayTrace)
        {
            if (_currBlock == null)
                return false;

            IBlockState state = mc.world.getBlockState(_currBlock);

            if (IsDoneBreaking(state) || mc.player.getDistanceSq(_currBlock) > Math.pow(range, range))
            {
                _currBlock = null;
                return false;
            }

            // CPacketAnimation
            mc.player.swingArm(EnumHand.MAIN_HAND);

            EnumFacing facing = EnumFacing.UP;

            if (rayTrace)
            {
                RayTraceResult result = mc.world.rayTraceBlocks(
                        new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                        new Vec3d(_currBlock.getX() + 0.5, _currBlock.getY() - 0.5,
                                _currBlock.getZ() + 0.5));

                if (result != null && result.sideHit != null)
                    facing = result.sideHit;
            }

            if (!_started)
            {
                _started = true;
                // Start Break

                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, _currBlock, facing));
            }
            else
            {
                mc.playerController.onPlayerDamageBlock(_currBlock, facing);
            }

            return true;
        }
    }
}
