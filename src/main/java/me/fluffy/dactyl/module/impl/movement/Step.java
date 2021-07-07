package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Step extends Module {
    public Setting<StepMode> stepModeSetting = new Setting<StepMode>("Mode", StepMode.AAC);
    public Setting<Double> height = new Setting<Double>("Height", 2.0D, 0.5D, 10.0D);
    public Setting<Boolean> entityStep = new Setting<Boolean>("EntityStep", true);
    public Setting<Boolean> useTimer = new Setting<Boolean>("UseTimer", false);


    public static Step INSTANCE;
    public Step() {
        super("Step", Category.MOVEMENT);
        INSTANCE = this;
    }



    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        switch((StepMode)stepModeSetting.getValue()) {
            case AAC:
                doStepAAC();
                break;
            case NORMAL:
                doStepNormal();
                break;
            case VANILLA:
                doStepVanilla();
                break;
        }
        this.setModuleInfo(getStepModInfo());
    }


    private void doStepAAC() {
        if (!mc.player.collidedHorizontally) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (!mc.player.onGround || mc.player.isOnLadder() || mc.player.isInWater() || mc.player.isInLava()) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (mc.player.movementInput.jump) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }

        double maxY2 = -1.0;
        final AxisAlignedBB grow2 = mc.player.getEntityBoundingBox().offset(0.0, 0.05, 0.0).grow(0.05);
        if (!mc.world.getCollisionBoxes(mc.player, grow2.offset(0.0, 2.0, 0.0)).isEmpty()) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        for (final AxisAlignedBB axisAlignedBB2 : mc.world.getCollisionBoxes(mc.player, grow2)) {
            if (axisAlignedBB2.maxY > maxY2) {
                maxY2 = axisAlignedBB2.maxY;
            }
        }
        final double n2 = maxY2 - mc.player.posY;
        if (n2 < 0.0 || n2 > 2.0) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if(useTimer.getValue()) {
            ((ITimer)mc.timer).setTickLength(150F);
        }
        if (n2 == 2.0 && (height.getValue() >= 2.0d)) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.78, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.63, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.51, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.9, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.21, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.45, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.43, mc.player.posZ, mc.player.onGround));
            mc.player.setPosition(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ);
        }
        if (n2 == 1.5 && (height.getValue() >= 1.5d)) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805212, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.24918707874468, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.1707870772188, mc.player.posZ, mc.player.onGround));
            mc.player.setPosition(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ);
        }
        if (n2 == 1.0 && (height.getValue() >= 1.0d)) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805212, mc.player.posZ, mc.player.onGround));
            mc.player.setPosition(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ);
        }
    }

    private void doStepVanilla() {
        if(mc.player.getRidingEntity() != null && entityStep.getValue()) {
            mc.player.getRidingEntity().stepHeight = height.getValue().floatValue();
        }
        mc.player.stepHeight = height.getValue().floatValue();
    }

    private void doStepNormal() {
        if (!mc.player.collidedHorizontally) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (!mc.player.onGround || mc.player.isOnLadder() || mc.player.isInWater() || mc.player.isInLava()) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if (mc.player.movementInput.jump) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        final AxisAlignedBB grow = mc.player.getEntityBoundingBox().offset(0.0, 0.05, 0.0).grow(0.05);
        if (!mc.world.getCollisionBoxes(mc.player, grow.offset(0.0, 1.0, 0.0)).isEmpty()) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        double maxY = -1.0;
        for (final AxisAlignedBB axisAlignedBB : mc.world.getCollisionBoxes(mc.player, grow)) {
            if (axisAlignedBB.maxY > maxY) {
                maxY = axisAlignedBB.maxY;
            }
        }
        final double n = maxY - mc.player.posY;
        if (n < 0.0 || n > 1.0) {
            if (useTimer.getValue()) {
                ((ITimer) mc.timer).setTickLength(50F);
            }
            return;
        }
        if(useTimer.getValue()) {
            ((ITimer)mc.timer).setTickLength(150F);
        }
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42 * n, mc.player.posZ, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.753 * n, mc.player.posZ, mc.player.onGround));
        mc.player.setPosition(mc.player.posX, mc.player.posY + 1.0 * n, mc.player.posZ);
    }

    @Override
    public void onDisable() {
        if(mc.player == null) {
            return;
        }
        if(mc.player.getRidingEntity() != null) {
            mc.player.getRidingEntity().stepHeight = 1f;
        }
        mc.player.stepHeight = 0.5f;
        if (useTimer.getValue()) {
            ((ITimer) mc.timer).setTickLength(50F);
        }
    }

    private String getStepModInfo() {
        switch((StepMode)stepModeSetting.getValue()) {
            case AAC:
                return "AAC";
            case NORMAL:
                return "Normal";
            case VANILLA:
                return "Vanilla";
        }
        return "";
    }



    public enum StepMode {
        NORMAL("Normal"),
        VANILLA("Vanilla"),
        AAC("AAC");

        public String name;

        private StepMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
