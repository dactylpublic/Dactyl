package me.chloe.dactyl.module.impl.combat;

import me.chloe.dactyl.util.HoleUtil;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.movement.Step;
import me.chloe.dactyl.module.impl.movement.Strafe;
import me.chloe.dactyl.module.impl.player.Freecam;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.CombatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Surround extends Module {
    public Setting<SurroundPage> pageSetting = new Setting<SurroundPage>("Page", SurroundPage.GENERAL);
    // general settings
    public Setting<Integer> blocksPerTick = new Setting<Integer>("Share", 4, 1, 12, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> doDelay = new Setting<Boolean>("EnableDelay", false, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Integer> milliDelay = new Setting<Integer>("Delay", 0, 0, 200, v->pageSetting.getValue()==SurroundPage.GENERAL && doDelay.getValue());
    public Setting<Boolean> autoCenter = new Setting<Boolean>("AutoCenter", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> packetPlace = new Setting<Boolean>("PacketPlace", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> antiCrystal = new Setting<Boolean>("AntiCrystal", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> nonLethalAttack = new Setting<Boolean>("NoLethal", true, v->pageSetting.getValue()==SurroundPage.GENERAL && antiCrystal.getValue());
    public Setting<Boolean> antiCrystalRotate = new Setting<Boolean>("AntiCRotate", false, v->pageSetting.getValue()==SurroundPage.GENERAL && antiCrystal.getValue());
    public Setting<Boolean> disableIfSafe = new Setting<Boolean>("DisableIfSafe", false, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> multiThreaded = new Setting<Boolean>("MultiThreaded", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> echestHoldPrio = new Setting<Boolean>("EChestHoldPrio", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> changeRightClickDelay = new Setting<Boolean>("UpdateRCD", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true, v->pageSetting.getValue()==SurroundPage.GENERAL);
    public Setting<Boolean> multiPointRotate = new Setting<Boolean>("MultiPoint", true, v->pageSetting.getValue()==SurroundPage.GENERAL && rotate.getValue());

    // disablers
    public Setting<Boolean> jumpDisable = new Setting<Boolean>("JumpDisable", true, v->pageSetting.getValue()==SurroundPage.DISABLERS);
    public Setting<Boolean> strafeDisable = new Setting<Boolean>("StrafeDisable", true, v->pageSetting.getValue()==SurroundPage.DISABLERS);
    public Setting<Boolean> stepDisable = new Setting<Boolean>("StepDisable", true, v->pageSetting.getValue()==SurroundPage.DISABLERS);
    public Setting<Boolean> moveDisable = new Setting<Boolean>("MoveDisable", false, v->pageSetting.getValue()==SurroundPage.DISABLERS);


    public static Surround INSTANCE;
    public Surround() {
        super("Surround", Category.COMBAT);
        INSTANCE = this;
    }

    private BlockPos basePos;
    private int offsetStep = 0;
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;
    private final TimeUtil timer = new TimeUtil();
    private ProtectionOffsetsThread protectionOffsetsThread;

    ArrayList<Vec3d> protectionOffsets = new ArrayList<>();

    @Override
    public void onClientUpdate() {
        if(mc.world == null || mc.player == null || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        doModInfo();
        doDisablers();

        if(offsetStep == 0) {
            basePos = (new BlockPos(mc.player.getPositionVector())).down();
            playerHotbarSlot = mc.player.inventory.currentItem;
        }
        if(!timer.hasPassed(milliDelay.getValue()) && doDelay.getValue()) {
            return;
        }
        if(!multiThreaded.getValue()) {
            protectionOffsets.addAll(CombatUtil.getProtectionOffsetsNew(antiCrystal.getValue()));
        }
        for (int i = 0; i < blocksPerTick.getValue(); i++) {
            try {
                if (this.offsetStep > protectionOffsets.size() - 1) {
                    endLoop();
                    return;
                }
                boolean isHoldingEchest = (echestHoldPrio.getValue() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).getBlock() == Blocks.ENDER_CHEST));
                int obi = CombatUtil.findSurroundBlock(isHoldingEchest);
                if (obi == -1) {
                    mc.player.inventory.currentItem = playerHotbarSlot;
                    mc.playerController.updateController();
                    this.toggle();
                    return;
                }
                if (mc.player.inventory.currentItem != obi) {
                    mc.player.inventory.currentItem = obi;
                    mc.playerController.updateController();
                }
                Vec3d offset = protectionOffsets.get(offsetStep);
                BlockPos placePosition = new BlockPos(this.basePos.add(offset.x, offset.y, offset.z));
                doAntiCrystal(placePosition);
                this.lastHotbarSlot = obi;
                CombatUtil.placeBlockSurroundNew(placePosition, false, rotate.getValue(), true, false, false, obi, packetPlace.getValue(), changeRightClickDelay.getValue(), multiPointRotate.getValue());
                this.offsetStep++;
            } catch(Exception exception) {}
        }
        timer.reset();
    }

    public class ProtectionOffsetsThread extends Thread {
        public void run() {
            while (Surround.INSTANCE.isEnabled()) {
                try {
                    protectionOffsets.clear();
                    protectionOffsets.addAll(CombatUtil.getProtectionOffsetsNew(antiCrystal.getValue()));
                    TimeUnit.MILLISECONDS.sleep(5L);
                } catch (Exception exception) {}
            }
        }
    }

    private void doAntiCrystal(BlockPos pos) {
        if(!antiCrystal.getValue()) {
            return;
        }
        if(isInterceptedByCrystal(pos)) {
            EntityEnderCrystal crystal = null;
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity == null || !(entity instanceof EntityEnderCrystal) || entity.isDead || mc.player.getDistance(entity) > 2.4D) {
                    continue;
                }
                crystal = (EntityEnderCrystal)entity;
            }
            if(crystal != null) {
                if(nonLethalAttack.getValue()) {
                    if(((double)CombatUtil.calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player) >= (mc.player.getHealth()+mc.player.getAbsorptionAmount()))) {
                        return;
                    }
                }
                if (antiCrystalRotate.getValue()) {
                    double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation((float)rots[0], (float)rots[1], mc.player.onGround));
                }
                mc.getConnection().sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                mc.getConnection().sendPacket(new CPacketUseEntity(crystal));
            }
        }
    }

    private boolean isInterceptedByCrystal(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityEnderCrystal && !entity.equals(mc.player) && !(entity instanceof EntityItem) && (new AxisAlignedBB(pos)).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private void endLoop() {
        this.offsetStep = 0;
        if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            mc.player.inventory.currentItem = this.playerHotbarSlot;
            mc.playerController.updateController();
            this.lastHotbarSlot = this.playerHotbarSlot;
        }
        if(disableIfSafe.getValue() && HoleUtil.isInAnyHole()) {
            this.toggle();
        }
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.player.inventory == null || mc.world == null) {
            return;
        }
        if(multiThreaded.getValue()) {
            protectionOffsetsThread = new ProtectionOffsetsThread();
            protectionOffsetsThread.start();
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
        if(mc.player == null || mc.player.inventory == null || mc.world == null) {
            return;
        }
        lastHotbarSlot = -1;
        playerHotbarSlot = -1;
        timer.reset();
    }

    private void doDisablers() {
        if((strafeDisable.getValue() && Strafe.INSTANCE.isEnabled()) || (stepDisable.getValue() && Step.INSTANCE.isEnabled()) || (moveDisable.getValue())) {
            if (mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F) {
                this.toggle();
                return;
            }
        }
        if(jumpDisable.getValue()) {
            if(mc.player.movementInput.jump) {
                this.toggle();
                return;
            }
        }
    }

    private void doModInfo() {
        this.setModuleInfo(HoleUtil.isInHole() ? TextFormatting.GREEN + "Safe" : TextFormatting.RED + "Unsafe");
    }

    public enum SurroundPage {
        GENERAL("General"),
        DISABLERS("Disablers");

        private final String name;

        private SurroundPage(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
