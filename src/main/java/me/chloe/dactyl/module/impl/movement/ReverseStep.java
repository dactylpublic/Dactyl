package me.chloe.dactyl.module.impl.movement;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.player.Freecam;
import me.chloe.dactyl.module.impl.player.JumpFill;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ReverseStep extends Module {
    Setting<StepMode> stepModeSetting = new Setting<StepMode>("Mode", StepMode.HOLES);
    Setting<Boolean> allWhenStep = new Setting<Boolean>("SwitchEnabled", true, "Switches mode to all when Step module is enabled.");
    //Setting<Boolean> inWater = new Setting<Boolean>("inWater", false, v->stepModeSetting.getValue()==StepMode.ALL);
    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT);
    }

    private final double[] oneblockPositions = new double[] { 0.42D, 0.75D };

    private int packets;

    private boolean jumped = false;

    @Override
    public void onClientUpdate() {
        if (mc.world == null || mc.player == null || (Strafe.INSTANCE.isEnabled() || Freecam.INSTANCE.isEnabled() || JumpFill.INSTANCE.isEnabled())) {
            return;
        }
        if(ElytraFly.INSTANCE.isEnabled()) {
            return;
        }
        this.setModuleInfo(stepModeSetting.getValue() == StepMode.ALL ? "All" : "Holes");
        boolean doSwitch = false;
        if(allWhenStep.getValue() && Step.INSTANCE.isEnabled()) {
            doSwitch = true;
        }
        if(stepModeSetting.getValue() == StepMode.HOLES && !doSwitch) {
            if (!mc.player.onGround) {
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    this.jumped = true;
            } else {
                this.jumped = false;
            }
            if (!this.jumped && mc.player.fallDistance < 0.5D && isInHole() && mc.player.posY - getNearestBlockBelow() <= 1.125D && mc.player.posY - getNearestBlockBelow() <= 0.95D && !isOnLiquid() && !isInLiquid()) {
                if (!mc.player.onGround)
                    this.packets++;
                if (!mc.player.onGround && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.isInsideOfMaterial(Material.LAVA) && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isOnLadder() && this.packets > 0) {
                    BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    for (double position : this.oneblockPositions) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position((blockPos.getX() + 0.5F), mc.player.posY - position, (blockPos.getZ() + 0.5F), true));
                    }
                    mc.player.setPosition((blockPos.getX() + 0.5F), getNearestBlockBelow() + 0.1D, (blockPos.getZ() + 0.5F));
                    this.packets = 0;
                }
            }
        } else if (stepModeSetting.getValue() == StepMode.ALL || doSwitch){
            if (mc.player == null || mc.world == null || (mc.player.isInLava() || mc.player.isInWater())) {
                return;
            }
            if (mc.player.onGround) {
                --mc.player.motionY;
            }
        }
    }


    private boolean isInHole() {
        BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        IBlockState blockState = mc.world.getBlockState(blockPos);
        return isBlockValid(blockState, blockPos);
    }
    private boolean isAboveHole() {
        BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY-1, mc.player.posZ);
        IBlockState blockState = mc.world.getBlockState(blockPos);
        return isBlockValid(blockState, blockPos);
    }

    private double getNearestBlockBelow() {
        for (double y = mc.player.posY; y > 0.0D; y -= 0.001D) {
            if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof net.minecraft.block.BlockSlab) && mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) != null)
                return y;
        }
        return -1.0D;
    }

    private double getNearestBlockBelowBelow() {
        for (double y = mc.player.posY-1; y > 0.0D; y -= 0.001D) {
            if (!(mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock() instanceof net.minecraft.block.BlockSlab) && mc.world.getBlockState(new BlockPos(mc.player.posX, y, mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(mc.world, new BlockPos(0, 0, 0)) != null)
                return y;
        }
        return -1.0D;
    }

    private boolean isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (blockState.getBlock() != Blocks.AIR)
            return false;
        if (mc.player.getDistanceSq(blockPos) < 1.0D)
            return false;
        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;
        if (mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR)
            return false;
        if(stepModeSetting.getValue() == StepMode.HOLES) {
            return (isBedrockHole(blockPos) || isObbyHole(blockPos) || isBothHole(blockPos) || isElseHole(blockPos));
        } else {
            return true;
        }
    }

    private boolean isObbyHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down() };
        for (BlockPos touching : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.OBSIDIAN)
                return false;
        }
        return true;
    }

    private boolean isBedrockHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down() };
        for (BlockPos touching : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK)
                return false;
        }
        return true;
    }

    private boolean isBothHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down() };
        for (BlockPos touching : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || (touchingState.getBlock() != Blocks.BEDROCK && touchingState.getBlock() != Blocks.OBSIDIAN))
                return false;
        }
        return true;
    }

    private boolean isElseHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks = { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down() };
        for (BlockPos touching : touchingBlocks) {
            IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || !touchingState.isFullBlock())
                return false;
        }
        return true;
    }

    private boolean isOnLiquid() {
        double y = mc.player.posY - 0.03D;
        for (int x = MathHelper.floor(mc.player.posX); x < MathHelper.ceil(mc.player.posX); x++) {
            for (int z = MathHelper.floor(mc.player.posZ); z < MathHelper.ceil(mc.player.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof net.minecraft.block.BlockLiquid)
                    return true;
            }
        }
        return false;
    }

    private boolean isInLiquid() {
        double y = mc.player.posY + 0.01D;
        for (int x = MathHelper.floor(mc.player.posX); x < MathHelper.ceil(mc.player.posX); x++) {
            for (int z = MathHelper.floor(mc.player.posZ); z < MathHelper.ceil(mc.player.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int)y, z);
                if (mc.world.getBlockState(pos).getBlock() instanceof net.minecraft.block.BlockLiquid)
                    return true;
            }
        }
        return false;
    }


    private enum StepMode {
        HOLES,
        ALL
    }
}
