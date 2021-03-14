package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.world.ClickBlockEvent;
import me.fluffy.dactyl.event.impl.world.DamageBlockEvent;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.event.impl.world.ResetBlockRemovingEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class MiningTweaks extends Module {
    public Setting<Boolean> noBreakAnimation = new Setting<Boolean>("NoBreakAnim", true);
    public Setting<MiningMode> modeSetting = new Setting<MiningMode>("Mode", MiningMode.PACKET);
    public Setting<Boolean> onlyPickaxe = new Setting<Boolean>("OnlyPickaxe", true, v -> modeSetting.getValue() == MiningMode.PACKET);
    public Setting<Boolean> reset = new Setting<Boolean>("Reset", true);
    public Setting<Boolean> strict = new Setting<Boolean>("Strict", true, v->modeSetting.getValue() == MiningMode.BYPASS);
    public Setting<Integer> exploitDelay = new Setting<Integer>("EDelay", 100, 10, 1000, v->modeSetting.getValue() == MiningMode.BYPASS && !strict.getValue());
    public Setting<Boolean> doCrystalPlace = new Setting<Boolean>("CrystalPlace", false, v->modeSetting.getValue() == MiningMode.BYPASS);
    public Setting<Boolean> fastBreakAll = new Setting<Boolean>("BreakOthers", true, v->modeSetting.getValue() == MiningMode.BYPASS);
    public Setting<Boolean> noBreakDelay = new Setting<Boolean>("AntiDelay", false);
    public Setting<Boolean> renderPacketBlock = new Setting<Boolean>("Render", true, v -> modeSetting.getValue() == MiningMode.PACKET);
    public Setting<Integer> resetRange = new Setting<Integer>("RemoveRange", 6, 1, 50, v -> modeSetting.getValue() == MiningMode.PACKET);
    public Setting<Boolean> autoTool = new Setting<Boolean>("AutoTool", false);

    public static MiningTweaks INSTANCE;
    public MiningTweaks() {
        super("BreakTweaks", Category.MISC);
        INSTANCE = this;
    }

    private boolean isMining = false;
    private boolean blockWasBroken = false;
    private BlockPos lastPos = null;
    private BlockPos lastBrokenPos = null;
    private Block lastBrokenBlock = null;
    private EnumFacing lastFacing = null;
    public BlockPos currentPos = null;
    public IBlockState currentBlockState = null;
    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil popbobTimer = new TimeUtil();
    boolean shit = false;

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(modeSetting.getValue() == MiningMode.SPEED ? "Speed" : "Util");
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (noBreakDelay.getValue()) {
            mc.playerController.blockHitDelay = 0;
        }
        if(modeSetting.getValue() == MiningMode.BYPASS) {
            if (shit == true && timer.hasPassed(exploitDelay.getValue().longValue()) && this.lastBrokenPos != null) {
                if(strict.getValue()) {
                    if(mc.world.getBlockState(this.lastBrokenPos).getBlock() != Blocks.AIR) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.lastBrokenPos, EnumFacing.DOWN));
                        blockWasBroken = false;
                        shit = false;
                    }
                } else {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.lastBrokenPos, EnumFacing.DOWN));
                    blockWasBroken = false;
                    shit = false;
                }
            }
            if (currentPos != null) {
                if (mc.world.getBlockState(currentPos).getBlock() == Blocks.AIR) {
                    this.lastBrokenPos = this.currentPos;
                    blockWasBroken = true;
                }
            }
        }

        if (currentPos != null) {
            if (mc.player != null && mc.player.getDistance(this.currentPos.getX(), this.currentPos.getY(), this.currentPos.getZ()) > ((resetRange.getValue()))) {
                this.currentPos = null;
                this.currentBlockState = null;
                return;
            }
            if (!mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) || mc.world.getBlockState(this.currentPos).getBlock() == Blocks.AIR) {
                this.currentPos = null;
                this.currentBlockState = null;
            }
        }
    }

    @SubscribeEvent
    public void onOtherBlockPlace(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if (modeSetting.getValue() == MiningMode.BYPASS) {
                if(event.getPacket() instanceof SPacketBlockChange) {
                    SPacketBlockChange packet = (SPacketBlockChange)event.getPacket();
                    if(fastBreakAll.getValue()) {
                        if(lastBrokenPos != null &&  packet.getBlockPosition().getX() == lastBrokenPos.getX() && packet.getBlockPosition().getY() == lastBrokenPos.getY() && packet.getBlockPosition().getZ() == lastBrokenPos.getZ()) {
                            if (packet.getBlockState().getBlock() != Blocks.AIR) {
                                //mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.lastBrokenPos, EnumFacing.DOWN));
                                popbobTimer.reset();
                                shit = true;
                            }
                        }
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(modeSetting.getValue() == MiningMode.BYPASS) {
                if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                    CPacketPlayerTryUseItemOnBlock p = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
                    if(!doCrystalPlace.getValue()) {
                        if (mc.player.getHeldItem(p.getHand()).getItem() == Items.END_CRYSTAL) {
                            return;
                        }
                    }
                    BlockPos placedPos = p.getPos().up();
                    if(lastBrokenPos != null &&  placedPos.getX() == lastBrokenPos.getX() && placedPos.getY() == lastBrokenPos.getY() && placedPos.getZ() == lastBrokenPos.getZ()) {
                        popbobTimer.reset();
                        shit = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (noBreakAnimation.getValue()) {
            if (event.getPacket() instanceof CPacketPlayerDigging) {
                CPacketPlayerDigging cPacketPlayerDigging = (CPacketPlayerDigging) event.getPacket();
                if (mc.world.getBlockState(cPacketPlayerDigging.getPosition()).getBlock() == Blocks.PORTAL)
                    return;
                for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(cPacketPlayerDigging.getPosition()))) {
                    if (entity instanceof EntityEnderCrystal) {
                        resetMining();
                        return;
                    }
                    if (entity instanceof EntityLivingBase) {
                        resetMining();
                        return;
                    }
                }
                if (cPacketPlayerDigging.getAction().equals(CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                    this.isMining = true;
                    setMiningInfo(cPacketPlayerDigging.getPosition(), cPacketPlayerDigging.getFacing());
                }
                if (cPacketPlayerDigging.getAction().equals(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    resetMining();
                }
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (renderPacketBlock.getValue() && this.currentPos != null && (modeSetting.getValue() == MiningMode.PACKET || modeSetting.getValue() == MiningMode.BYPASS)) {
            Color color = new Color(this.timer.hasPassed((int)(2000.0f * (20F / Dactyl.tickRateManager.getTickRate()))) ? 0 : 255, this.timer.hasPassed((int) (2000.0f * (20F / Dactyl.tickRateManager.getTickRate()))) ? 255 : 0, 0, 255);
            RenderUtil.drawBoxESP(this.currentPos, color, 1.0f, true, true, 44);
        }
    }

    @SubscribeEvent
    public void resetBlockDamage(ResetBlockRemovingEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if(reset.getValue() && modeSetting.getValue() != MiningMode.NONE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void damageBlock(DamageBlockEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if (mc.world.getBlockState(event.getPos()).getBlock() == Blocks.PORTAL) {
            return;
        }
        if (canBreak(event.getPos())) {
            if(onlyPickaxe.getValue() && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
                return;
            }
            if (this.reset.getValue()) {
                mc.playerController.isHittingBlock = false;
            }
            switch ((MiningMode)modeSetting.getValue()) {
                case SPEED:
                    if (mc.playerController.curBlockDamageMP >= 0.7F) {
                        mc.playerController.curBlockDamageMP = 1.0F;
                    }
                    break;
                case PACKET:
                    if (this.currentPos == null) {
                        this.currentPos = event.getPos();
                        this.currentBlockState = mc.world.getBlockState(this.currentPos);
                        this.timer.reset();
                    }
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    //mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    event.setCanceled(true);
                    break;
                case BYPASS:
                    if (this.currentPos == null) {
                        this.currentPos = event.getPos();
                        this.currentBlockState = mc.world.getBlockState(this.currentPos);
                        this.timer.reset();
                    }
                    this.lastBrokenBlock = mc.world.getBlockState(currentPos).getBlock();
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), EnumFacing.UP));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), EnumFacing.DOWN));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), EnumFacing.UP));
                    event.setCanceled(true);
                    break;
                case INSTANT:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.playerController.onPlayerDestroyBlock(event.getPos());
                    mc.world.setBlockToAir(event.getPos());
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if(mc.player == null || mc.world == null || event.getPos() == null || mc.world.getBlockState(event.getPos()) == null) {
            return;
        }
        if(autoTool.getValue()) {
            int bestSlot = -1;
            double max = 0;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                float speed = stack.getDestroySpeed(mc.world.getBlockState(event.getPos()));
                int eff;
                if (speed > 1) {
                    speed += ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                    if (speed > max) {
                        max = speed;
                        bestSlot = i;
                    }
                }
            }
            if (bestSlot != -1) {
                mc.player.inventory.currentItem = bestSlot;
                int i = this.mc.player.inventory.currentItem;
                if (i != mc.playerController.currentPlayerItem) {
                    mc.playerController.currentPlayerItem = i;
                    mc.playerController.connection.sendPacket(new CPacketHeldItemChange(mc.playerController.currentPlayerItem));
                }
            }
        }
    }

    @SubscribeEvent
    public void clickBlock(ClickBlockEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if (this.reset.getValue() && mc.playerController.curBlockDamageMP > 0.1F && modeSetting.getValue() != MiningMode.NONE) {
            mc.playerController.isHittingBlock = true;
        }
    }

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if (mc.player == null || mc.player.connection == null || mc.world == null) {
            return;
        }
        if(event.getStage() == ForgeEvent.Stage.PRE) {
            if(modeSetting.getValue() != MiningMode.NONE) {
                mc.playerController.blockHitDelay = 0;
                if (reset.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    mc.playerController.isHittingBlock = false;
                }
            }
        }
        if (noBreakAnimation.getValue()) {
            if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
                resetMining();
                return;
            }
            if (this.isMining && this.lastPos != null && this.lastFacing != null) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
            }
        }
    }

    @Override
    public void onDisable() {
        blockWasBroken = false;
        lastBrokenPos = null;
        lastBrokenBlock = null;
        shit = false;
        popbobTimer.reset();
    }

    private void setMiningInfo(BlockPos lastPos, EnumFacing lastFacing) {
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void resetMining() {
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
    }

    private boolean canBreak(BlockPos pos) {
        IBlockState blockState = mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return (block.getBlockHardness(blockState, (World)mc.world, pos) != -1.0F);
    }

    public enum MiningMode {
        PACKET("Packet"),
        BYPASS("Bypass"),
        NONE("None"),
        INSTANT("Instant"),
        SPEED("Speed");

        private final String name;
        private MiningMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}