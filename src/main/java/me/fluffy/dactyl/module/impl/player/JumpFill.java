package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class JumpFill extends Module {
    //Setting<RubberbandMode> rubberbandModeSetting = new Setting<RubberbandMode>("Mode", RubberbandMode.NCP);
    Setting<Priority> prioritySetting = new Setting<Priority>("Prio", Priority.OBI);
    Setting<Boolean> packetSwitch = new Setting<Boolean>("PacketSwitch", true);
    Setting<Integer> timerDelay = new Setting<Integer>("Delay", 300, 25, 1000);
    Setting<Boolean> rotate = new Setting<Boolean>("Rotate", false);
    public JumpFill() {
        super("ReverseFill", Category.PLAYER);
    }

    private final TimeUtil timer = new TimeUtil();

    int oldSlot = -1;
    int blockSlot = -1;

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!mc.player.onGround) {
            this.toggle();
            return;
        }
        switch(prioritySetting.getValue()) {
            case OBI:
                if(CombatUtil.findBlockInHotbar(Blocks.OBSIDIAN) == -1) {
                    this.toggle();
                    return;
                } else {
                    blockSlot = CombatUtil.findBlockInHotbar(Blocks.OBSIDIAN);
                }
                break;
            case STONE:
                if(CombatUtil.findBlockInHotbar(Blocks.STONE) == -1) {
                    this.toggle();
                    return;
                } else {
                    blockSlot = CombatUtil.findBlockInHotbar(Blocks.STONE);
                }
                break;
            case COBBLESTONE:
                if(CombatUtil.findBlockInHotbar(Blocks.COBBLESTONE) == -1) {
                    this.toggle();
                    return;
                } else {
                    blockSlot = CombatUtil.findBlockInHotbar(Blocks.COBBLESTONE);
                }
                break;
        }
        if(blockSlot == -1) {
            this.toggle();
            return;
        }
        oldSlot = mc.player.inventory.currentItem;
        if(packetSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));
        } else {
            mc.player.inventory.currentItem = blockSlot;
        }
        mc.player.jump();
        timer.reset();
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(timer.hasPassed(timerDelay.getValue().longValue())) {
            BlockPos targetPos = new BlockPos(mc.player.getPositionVector()).add(0, -1, 0);
            boolean placedBlock = CombatUtil.placeBlock(targetPos, false, rotate.getValue(), true, false, false, 0);
            if(placedBlock) {
                mc.player.onGround = false;
                mc.player.motionY = 20.0;
            }
            this.toggle();
        }
    }

    @Override
    public void onDisable() {
        if(oldSlot != -1) {
            if (packetSwitch.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            } else {
                mc.player.inventory.currentItem = oldSlot;
            }
        }
        blockSlot = -1;
        oldSlot = -1;
        timer.reset();
    }

    private int getSlot(Priority priority) {
        switch(priority) {
            case OBI:
                return CombatUtil.findBlockInHotbar(Blocks.OBSIDIAN);
            case STONE:
                return CombatUtil.findBlockInHotbar(Blocks.STONE);
            case COBBLESTONE:
                return CombatUtil.findBlockInHotbar(Blocks.COBBLESTONE);
        }
        return -1;
    }


    private enum Priority {
        OBI,
        STONE,
        COBBLESTONE
    }
}
