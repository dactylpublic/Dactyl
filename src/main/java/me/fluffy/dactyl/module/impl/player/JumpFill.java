package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.combat.Surround;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JumpFill extends Module {
    Setting<Priority> prioritySetting = new Setting<Priority>("Prio", Priority.OBI);
    //Setting<Boolean> packetSwitch = new Setting<Boolean>("PacketSwitch", false);
    //Setting<Integer> packetOffset = new Setting<Integer>("POffset", 5, 1, 100);
    Setting<Integer> packetOffset = new Setting<Integer>("POffset", 5, 1, 100);
    Setting<Boolean> rotate = new Setting<Boolean>("Rotate", false);
    public static JumpFill INSTANCE;
    public JumpFill() {
        super("ReverseFill", Category.PLAYER);
        INSTANCE = this;
    }

    boolean isHoldingBlock = false;
    int blockSlot = -1;
    private final TimeUtil timer = new TimeUtil();

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!CombatUtil.checkCanPlaceBurrow(new BlockPos(mc.player.getPositionVector()))) {
            this.disable();
            return;
        }
        if(!mc.player.onGround) {
            this.disable();
            return;
        }
        timer.reset();
        isHoldingBlock = false;
        blockSlot = -1;
        if(mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            isHoldingBlock = true;
            blockSlot = mc.player.inventory.currentItem;
            CombatUtil.switchToSlot(true, CombatUtil.findNonBlockInHotbar());
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            this.disable();
            return;
        }
        if(isHoldingBlock && blockSlot == -1) {
            return;
        }
        if(timer.hasPassed((isHoldingBlock ? (75) : 0))) {
            if(!mc.player.onGround) {
                this.disable();
                return;
            }
            double startX = mc.player.posX;
            double startY = mc.player.posY;
            double startZ = mc.player.posZ;

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, mc.player.onGround));
            BlockPos targetPos = new BlockPos(mc.player.getPositionVector()).add(0, 0, 0);
            int oldslot = isHoldingBlock ? blockSlot : mc.player.inventory.currentItem;
            int switchSlot = (isHoldingBlock ? blockSlot : CombatUtil.findBlockInHotbar(getBlockSetting()));
            boolean placedBlock = CombatUtil.placeBlockBurrow(targetPos, false, rotate.getValue(), false, (switchSlot != -1), true, switchSlot);
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            if(switchSlot == -1) {
                this.disable();
                return;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + packetOffset.getValue(), mc.player.posZ, mc.player.onGround));
            if(switchSlot != 420) {
                CombatUtil.switchToSlot(true, oldslot);
            }
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        timer.reset();
        isHoldingBlock = false;
        blockSlot = -1;
    }

    private Block getBlockSetting() {
        if(mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            return Block.getBlockFromItem(mc.player.getHeldItemMainhand().getItem());
        }
        switch(prioritySetting.getValue()) {
            case OBI:
                return Blocks.OBSIDIAN;
            case STONE:
                return Blocks.STONE;
            case COBBLESTONE:
                return Blocks.COBBLESTONE;
        }
        return Blocks.ENDER_CHEST;
    }


    private enum Priority {
        OBI,
        STONE,
        COBBLESTONE
    }
}
