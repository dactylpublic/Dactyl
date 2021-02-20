package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class LongJump extends Module {
    public Setting<Double> boost = new Setting<Double>("Boost", 4.0D, 0.1D, 10.0D);
    public Setting<Boolean> autoSprint = new Setting<Boolean>("AutoSprint", false);
    public Setting<Boolean> autoOff = new Setting<Boolean>("AutoOff", false);
    public Setting<Boolean> timeOut = new Setting<Boolean>("Timeout", false);
    public Setting<Integer> timeOutDelay = new Setting<Integer>("TimeoutDelay", 150, 50, 1000, v->timeOut.getValue());


    public static LongJump INSTANCE;
    public LongJump() {
        super("LongJump", Category.MOVEMENT);
        INSTANCE = this;
    }

    private final TimeUtil timer = new TimeUtil();
    private int stage = 0;
    private double moveSpeed, lastDist;
    private boolean didJump = false;

    @Override
    public void onToggle() {
        timer.reset();
        didJump = false;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook p = (SPacketPlayerPosLook)event.getPacket();
                if(mc.player.getDistance(p.getX(), p.getY(), p.getZ()) >= 0.75d) {
                    if(autoOff.getValue()) {
                        this.toggle();
                    }
                }
            }
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc == null || mc.player == null) {
            return;
        }
        lastDist = Math.sqrt(((mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)) + ((mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)));
        if(canSprint() && autoSprint.getValue()) {
            mc.player.setSprinting(true);
        }
        //if(didJump && autoOff.getValue() && mc.player.onGround) {
        //    this.toggle();
        //}
        this.setModuleInfo(timeOut.getValue() ? "Timeout" : "Boost");
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        if(mc.player.isElytraFlying() || mc.player.isInLava() || mc.player.isInWater()) {
            return;
        }
        if(timeOut.getValue() && !timer.hasPassed(timeOutDelay.getValue().longValue())) {
            return;
        }
        switch (stage) {
            case 0:
                ++stage;
                lastDist = 0.0D;
                break;
            case 2:
                double motionY = 0.40123128;
                if ((mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F) && mc.player.onGround) {
                    if (mc.player.isPotionActive(MobEffects.JUMP_BOOST))
                        motionY += ((mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
                    event.setY(mc.player.motionY = motionY);
                    moveSpeed *= 2.149;
                }
                break;
            case 3:
                moveSpeed = lastDist - (0.76 * (lastDist - getBaseMoveSpeed()));
                break;
            default:
                if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0D, mc.player.motionY, 0.0D)).size() > 0 || mc.player.collidedVertically) && stage > 0) {
                    stage = mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F ? 0 : 1;
                }
                moveSpeed = lastDist - lastDist / 159.0D;
                break;
        }
        moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
        double forward = mc.player.movementInput.moveForward, strafe = mc.player.movementInput.moveStrafe, yaw = mc.player.rotationYaw;
        if (forward != 0 && strafe != 0) {
            forward = forward * Math.sin(Math.PI / 4);
            strafe = strafe * Math.cos(Math.PI / 4);
        } else {
            event.setX(0);
            event.setZ(0);
        }
        if((forward * moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.99D != 0 || (forward * moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.99D != 0) {
            didJump = true;
        }
        event.setX((forward * moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.99D);
        event.setZ((forward * moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.99D);
        ++stage;
        timer.reset();
    }


    private double getBaseMoveSpeed() {
        double baseSpeed = 0.272;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier+1));
        }
        return baseSpeed*(boost.getValue());
    }


    private boolean canSprint() {
        return ((mc.player.movementInput.moveStrafe != 0.0F || mc.player.moveForward != 0.0F) && !mc.player.isActiveItemStackBlocking() && !mc.player.isOnLadder() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6);
    }
}
