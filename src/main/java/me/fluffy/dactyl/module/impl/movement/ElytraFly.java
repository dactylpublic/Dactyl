package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.PlayerTravelEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ISPacketPlayerPosLook;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.MathUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFly extends Module {
    public Setting<ElytraMode> modeSetting = new Setting<ElytraMode>("Mode", ElytraMode.UPRIGHT);
    public Setting<Double> speed = new Setting<Double>("Speed", 18.0D, 0.1D, 25.0D);
    public Setting<Double> downSpeed = new Setting<Double>("DownSpeed", 10.0D, 0.1D, 25.0D, v->modeSetting.getValue() != ElytraMode.TOOBEE);
    public Setting<Double> glideSpeed = new Setting<Double>("GlideSpeed", 5.0d, 1.0d, 6.0d, v->modeSetting.getValue() != ElytraMode.TOOBEE);
    public Setting<Boolean> autoEquip = new Setting<Boolean>("AutoEquip", true);
    public Setting<Boolean> autoTakeOff = new Setting<Boolean>("AutoTakeoff", true);
    public Setting<Boolean> timerTakeOff = new Setting<Boolean>("Timer", true, v->autoTakeOff.getValue());
    public Setting<Boolean> noRotate = new Setting<Boolean>("NoRotate", true);
    public static ElytraFly INSTANCE;
    public ElytraFly() {
        super("ElytraFly", Category.MOVEMENT);
        INSTANCE = this;
    }

    private final TimeUtil instantTimer = new TimeUtil();
    private int elytraSlot = -1;

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(modeSetting.getValue().toString());
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(autoTakeOff.getValue()) {
            if(timerTakeOff.getValue() && !mc.player.isElytraFlying()) {
                if(mc.player.onGround) {
                    ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50 / 0.2f);
                }
            }
            if(mc.player.onGround) {
                mc.player.jump();
            }
        }
    }

    @SubscribeEvent
    public void onTravel(PlayerTravelEvent event) {
        if(mc.player == null) {
            return;
        }

        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }

        if(!mc.player.isElytraFlying()) {
            if(!mc.player.onGround && autoTakeOff.getValue()) {
                if(!instantTimer.hasPassed(100)) {
                    return;
                }
                instantTimer.reset();
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }
            return;
        } else {
            ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
        }
        switch (modeSetting.getValue()) {
            case TOOBEE:
                doBoostFly();
                break;
            case UPRIGHT:
                doUpRightFly(event);
                break;
            case CREATIVE:
                doCreativeFly(event);
                break;

        }
    }

    private void doCreativeFly(PlayerTravelEvent event) {
        if (mc.player.movementInput.jump) {
            mc.player.motionY = (speed.getValue()/10);
            event.setCanceled(true);
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        event.setCanceled(true);

        double[] dir = MathUtil.directionSpeed(speed.getValue()/10);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionY = -(glideSpeed.getValue() / 10000f);
            mc.player.motionZ = dir[1];
        }

        if (mc.player.movementInput.sneak) {
            mc.player.motionY = -(downSpeed.getValue()/10);
        }

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    private void doUpRightFly(PlayerTravelEvent event) {
        if (mc.player.movementInput.jump) {
            double l_MotionSq = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);

            if (l_MotionSq > 1.0)
            {
                return;
            }
            else
            {
                double[] dir = MathUtil.directionSpeedNoForward(speed.getValue()/10);

                mc.player.motionX = dir[0];
                mc.player.motionY = -(glideSpeed.getValue() / 10000f);
                mc.player.motionZ = dir[1];
            }

            event.setCanceled(true);
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        event.setCanceled(true);

        double[] dir = MathUtil.directionSpeed(speed.getValue()/10);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionY = -(glideSpeed.getValue() / 10000f);
            mc.player.motionZ = dir[1];
        }

        if (mc.player.movementInput.sneak) {
            mc.player.motionY = -(downSpeed.getValue()/10);
        }

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    private void doBoostFly() {
        if (this.mc.player.isElytraFlying() && this.mc.world.getBlockState(this.mc.player.getPosition().add(0.0D, -0.1D, 0.0D)).getMaterial() instanceof MaterialLiquid && this.mc.world.getBlockState(this.mc.player.getPosition().add(0, 1, 0)).getBlock() == Blocks.AIR && this.mc.player.motionY > 0.0D) {
            this.mc.player.addVelocity(0.0D, 0.05D, 0.0D);
        }

        if (!this.mc.player.isElytraFlying() || this.mc.player.motionY > -0.09D) {
            return;
        }

        double s;

        for (s = Math.abs(this.mc.player.motionX) + Math.abs(this.mc.player.motionY) + Math.abs(this.mc.player.motionZ); s > (speed.getValue()/10); s = Math.abs(this.mc.player.motionX) + Math.abs(this.mc.player.motionY) + Math.abs(this.mc.player.motionZ)) {
            mc.player.motionX *= 0.95D;
            mc.player.motionY *= 0.95D;
            mc.player.motionZ *= 0.95D;
        }

        Vec3d vec3d = (new Vec3d(0.0D, 0.0D, 0.23D)).rotatePitch(-((float) Math.toRadians(this.mc.player.rotationPitch))).rotateYaw(-((float) Math.toRadians(this.mc.player.rotationYaw)));

        if (MathHelper.clamp(s / 2.0D, 0.0D, (speed.getValue()/10) - 0.25D) < 0.23D)
        {
            vec3d = vec3d.scale(0.2D);
        }

        this.mc.player.addVelocity(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
        elytraSlot = -1;
        if (autoEquip.getValue()) {
            if (mc.player != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                for (int i = 0; i < 44; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty() || stack.getItem() != Items.ELYTRA) {
                        continue;
                    }
                    elytraSlot = i;
                    break;
                }

                if (elytraSlot != -1) {
                    boolean armorInChest = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.AIR;

                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, elytraSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);

                    if (armorInChest) {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, elytraSlot, 0, ClickType.PICKUP, mc.player);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
        if (elytraSlot != -1) {
            boolean hasElytra = !mc.player.inventory.getStackInSlot(elytraSlot).isEmpty() || mc.player.inventory.getStackInSlot(elytraSlot).getItem() != Items.AIR;

            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, elytraSlot, 0, ClickType.PICKUP, mc.player);

            if (hasElytra) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.player == null) {
            return;
        }
        if(noRotate.getValue()) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                ((ISPacketPlayerPosLook) packet).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook) packet).setPitch(mc.player.rotationPitch);
            }
        }
    }

    @Override
    public void onToggle() {
        instantTimer.reset();
    }


    public enum ElytraMode {
        TOOBEE("MaxBoost"),
        CREATIVE("Creative"),
        UPRIGHT("Upright");

        private final String name;
        private ElytraMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
