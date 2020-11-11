package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.player.Freecam;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Surround extends Module {
    public Setting<Integer> quota = new Setting<Integer>("Quota", 4, 1, 10);
    public Setting<Integer> delay = new Setting<Integer>("Delay", 50, 0, 150);
    public Setting<Boolean> allowEating = new Setting<Boolean>("AllowEating", true, "Slower surround if turned on, faster surround if turned off");
    public Setting<Boolean> autoCenter = new Setting<Boolean>("AutoCenter", true);
    public Setting<Boolean> autoDisable = new Setting<Boolean>("AutoDisable", false);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);


    private BlockPos basePos;
    private int offsetStep = 0;
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;

    private TimeUtil timer = new TimeUtil();

    public Surround() {
        super("Surround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if(mc.player == null) {
            return;
        }
        if(autoCenter.getValue()) {
            CombatUtil.centerToNearestblock();
        }
        playerHotbarSlot = mc.player.inventory.currentItem;
        lastHotbarSlot = -1;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if(mc.player == null) {
            return;
        }
        lastHotbarSlot = -1;
        playerHotbarSlot = -1;
        timer.reset();
    }

    @Override
    public void onClientUpdate() {
        if(mc.world == null || mc.player == null || Freecam.INSTANCE.isEnabled()) {
            return;
        }

        if(this.offsetStep == 0) {
            this.basePos = (new BlockPos(mc.player.getPositionVector())).down();
            this.playerHotbarSlot = mc.player.inventory.currentItem;
        }
        if(!timer.hasPassed(delay.getValue())) {
            return;
        }
        for (int i = 0; i < (int)quota.getValue(); i++) {
            if (this.offsetStep >= CombatUtil.protectionoffsets.length) {
                endLoop();
                return;
            }
            int obi = CombatUtil.findBlockInHotbar(Blocks.OBSIDIAN);
            if(obi == -1) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
                this.toggle();
                return;
            }
            Vec3d offset = CombatUtil.protectionoffsets[this.offsetStep];
            BlockPos placePosition = new BlockPos(this.basePos.add(offset.x, offset.y, offset.z));
            this.lastHotbarSlot = obi;
            CombatUtil.placeBlock(placePosition, false, rotate.getValue(), true, true, allowEating.getValue(), obi);
            this.offsetStep++;
        }
        timer.reset();
    }

    private void endLoop() {
        this.offsetStep = 0;
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if(allowEating.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(this.playerHotbarSlot));
            } else {
                mc.player.inventory.currentItem = this.playerHotbarSlot;
            }
            this.lastHotbarSlot = this.playerHotbarSlot;
        }
        if(autoDisable.getValue()) {
            this.toggle();
        }
    }
}
