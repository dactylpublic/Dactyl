package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class Strafe extends Module {
    Setting<Boolean> useTimer = new Setting<Boolean>("UseTimer", true);
    Setting<Boolean> autoSprint = new Setting<Boolean>("AutoSprint", true);
    Setting<SkipMode> skipModeSetting = new Setting<SkipMode>("Skips", SkipMode.TICK);
    Setting<Integer> skipTickHops = new Setting<Integer>("SkipTicks", 1, 1, 10, vis->skipModeSetting.getValue() == SkipMode.TICK);
    Setting<Integer> skipDelayHops = new Setting<Integer>("SkipDelay", 25, 1, 100, vis->skipModeSetting.getValue() == SkipMode.MS);
    public Strafe() {
        super("Strafe", Category.MOVEMENT);
    }

    private int stage = 0;
    private int cooldown = 0;
    private double moveSpeed, lastDist;

    private final TimeUtil timer = new TimeUtil();


    @Override
    public void onClientUpdate() {
        if(mc == null || mc.player == null) {
            return;
        }
        lastDist = Math.sqrt(((mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)) + ((mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)));
        if(canSprint() && autoSprint.getValue()) {
            mc.player.setSprinting(true);
        }
        this.setModuleInfo(skipModeSetting.getValue() != SkipMode.NONE ? "Skips" : "Bhop");
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        //if(mc.player.isInWeb) {
        //    return;
        //}
        cooldown++;
        switch((SkipMode)skipModeSetting.getValue()) {
            case TICK:
                if(cooldown < skipTickHops.getValue()) {
                    return;
                }
                break;
            case MS:
                if(!timer.hasPassed(skipDelayHops.getValue())) {
                    return;
                }
                break;
            default:
                break;
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
            if(useTimer.getValue()) {
                ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
            }
            event.setX(0);
            event.setZ(0);
        }
        event.setX((forward * moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.99D);
        event.setZ((forward * moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.99D);
        ++stage;
        cooldown = 0;
        timer.reset();
    }

    @Override
    public void onToggle() {
        ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
        cooldown = 0;
        timer.reset();
    }


    private enum SkipMode {
        TICK,
        MS,
        NONE
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.272;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier+1));
        }
        if(useTimer.getValue()) {
            ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50 / 1.088f);
        }
        return baseSpeed;
    }


    private boolean canSprint() {
        return ((mc.player.movementInput.moveStrafe != 0.0F || mc.player.moveForward != 0.0F) && !mc.player.isActiveItemStackBlocking() && !mc.player.isOnLadder() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6);
    }
}
