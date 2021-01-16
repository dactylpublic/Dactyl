package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JumpFill extends Module {
    Setting<Priority> prioritySetting = new Setting<Priority>("Prio", Priority.OBI);
    Setting<Boolean> packetSwitch = new Setting<Boolean>("PacketSwitch", false);
    Setting<Boolean> rotate = new Setting<Boolean>("Rotate", false);
    public static JumpFill INSTANCE;
    public JumpFill() {
        super("ReverseFill", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!mc.player.onGround) {
            this.toggle();
            return;
        }
        double startX = mc.player.posX;
        double startY = mc.player.posY;
        double startZ = mc.player.posZ;
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805212, mc.player.posZ, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.166109260938215, mc.player.posZ, mc.player.onGround));
        BlockPos targetPos = new BlockPos(mc.player.getPositionVector()).add(0, 0, 0);
        int oldslot = mc.player.inventory.currentItem;
        int switchSlot = CombatUtil.findBlockInHotbar(getBlockSetting());
        boolean placedBlock = CombatUtil.placeBlockBurrow(targetPos, false, rotate.getValue(), true, (switchSlot != -1), packetSwitch.getValue(), switchSlot);
        if(switchSlot == -1) {
            this.toggle();
        }
        if(placedBlock) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805212, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(startX, startY, startZ, true));
        }
        CombatUtil.switchToSlot(packetSwitch.getValue(), oldslot);
        this.toggle();
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
