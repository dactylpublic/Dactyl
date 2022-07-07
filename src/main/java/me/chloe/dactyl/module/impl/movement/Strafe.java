package me.chloe.dactyl.module.impl.movement;

import me.chloe.dactyl.event.ForgeEvent;
import me.chloe.dactyl.event.impl.player.MoveEvent;
import me.chloe.dactyl.injection.inj.access.IMinecraft;
import me.chloe.dactyl.injection.inj.access.ITimer;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.CombatUtil;
import me.chloe.dactyl.util.MathUtil;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.event.impl.world.EntityRemovedEvent;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.player.Freecam;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

public class Strafe extends Module {
    Setting<Boolean> useTimer = new Setting<Boolean>("UseTimer", true);
    Setting<Boolean> autoSprint = new Setting<Boolean>("AutoSprint", true);
    Setting<Boolean> addSpeed = new Setting<Boolean>("Custom", false);
    Setting<Boolean> strictAirEating = new Setting<Boolean>("StrictAirEat", false);
    Setting<String> speedAddition = new Setting<String>("SpeedAddition", "0.272", v->addSpeed.getValue());
    Setting<Boolean> extraSpeed = new Setting<Boolean>("Extra", false);
    Setting<Boolean> damageBoost = new Setting<Boolean>("DMGBoost", false);
    Setting<Integer> sneakSpeed = new Setting<Integer>("SneakPercent", 90, 1, 100);
    Setting<Double> vanillaSpeed = new Setting<Double>("VanillaSpeed", 6.0d, 0.1d, 10.0d, vis->extraSpeed.getValue());
    Setting<SkipMode> skipModeSetting = new Setting<SkipMode>("Skips", SkipMode.TICK);
    Setting<Integer> skipTickHops = new Setting<Integer>("SkipTicks", 1, 1, 10, vis->skipModeSetting.getValue() == SkipMode.TICK);
    Setting<Integer> skipDelayHops = new Setting<Integer>("SkipDelay", 25, 1, 100, vis->skipModeSetting.getValue() == SkipMode.MS);
    Setting<Boolean> fastSwim = new Setting<Boolean>("FastSwim", true);
    Setting<Double> fastSwimXZWater = new Setting<Double>("WaterXZ", 4.4D, 1.0D, 10.0D, v->fastSwim.getValue());
    Setting<Double> fastSwimYWater = new Setting<Double>("WaterY", 1.5D, 1.0D, 10.0D, v->fastSwim.getValue());
    Setting<Double> fastSwimXZLava = new Setting<Double>("LavaXZ", 4.4D, 1.0D, 10.0D, v->fastSwim.getValue());
    Setting<Double> fastSwimYLava = new Setting<Double>("LavaY", 1.5D, 1.0D, 10.0D, v->fastSwim.getValue());

    public static Strafe INSTANCE;
    public Strafe() {
        super("Strafe", Category.MOVEMENT);
        INSTANCE = this;
    }

    private int stage = 0;
    private int cooldown = 0;
    private double moveSpeed, lastDist;
    private double damageMultiplier = 0;

    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil damageTimer = new TimeUtil();

    private boolean wasSneak = false;

    @SubscribeEvent
    public void onEntityRemove(EntityRemovedEvent event) {
        if(mc.world != null && mc.player != null && damageBoost.getValue() && event.getEntity() != null && event.getEntity() instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) event.getEntity();
            if(mc.player.getDistance(crystal) >= 13) {
                return;
            }
            float playerDMG = CombatUtil.calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player);
            if(playerDMG >= 3.5f && ((1+(playerDMG/36) > damageMultiplier))) {
                damageMultiplier = 1+(playerDMG/36);
                damageTimer.reset();
            }
        }
    }

    @SubscribeEvent
    public void onDamageTaken(LivingAttackEvent event) {
        if(mc.player == null || mc.world == null || event.getEntity() != mc.player) {
            return;
        }
        if(!(event.getAmount() >= 36) && event.getAmount() >= 3.5f && ((1+(event.getAmount()/36) > damageMultiplier))) {
            damageMultiplier = 1+(event.getAmount()/36);
            damageTimer.reset();
        }
    }


    @Override
    public void onClientUpdate() {
        if(mc == null || mc.player == null) {
            return;
        }
        if(strictAirEating.getValue()) {
            if (mc.player.isHandActive() && !mc.player.isRiding()) {
                if(!wasSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    wasSneak = true;
                }
            } else {
                if(wasSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    wasSneak = false;
                }
            }
        }
        if(damageTimer.hasPassed(1750)) {
            damageMultiplier = 1.0d;
            damageTimer.reset();
        }

        lastDist = Math.sqrt(((mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX)) + ((mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ)));
        if(canSprint() && autoSprint.getValue()) {
            mc.player.setSprinting(true);
        }
        String setModInfo = (skipModeSetting.getValue() != SkipMode.NONE ? "Skips" : "Fast");
        if(damageBoost.getValue()) {
            setModInfo = "Boost";
        }
        this.setModuleInfo(setModInfo);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        if(Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if(LongJump.INSTANCE.isEnabled()) {
            return;
        }
        if(mc.player.isElytraFlying() || mc.player.isInLava() || mc.player.isInWater() || ElytraFly.INSTANCE.isEnabled()) {
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
        if(extraSpeed.getValue()) {
            double[] vanillaCalc = MathUtil.directionSpeed(vanillaSpeed.getValue()/10);
            event.setX(vanillaCalc[0]);
            event.setZ(vanillaCalc[1]);
        }
        ++stage;
        cooldown = 0;
        timer.reset();
    }

    @SubscribeEvent
    public void onMoveSwim(MoveEvent event) {
        if(!(mc.player.isInWater() || mc.player.isInLava())) {
            return;
        }
        if(!fastSwim.getValue()) {
            return;
        }
        if(ElytraFly.INSTANCE.isEnabled()) {
            return;
        }
        if(mc.player.isInWater()) {
            event.setX(event.getX()*fastSwimXZWater.getValue());
            if(!Jesus.INSTANCE.isEnabled()) {
                event.setY(event.getY() * fastSwimYWater.getValue());
            }
            event.setZ(event.getZ()*fastSwimXZWater.getValue());
        } else if(mc.player.isInLava()) {
            event.setX(event.getX()*fastSwimXZLava.getValue());
            if(!Jesus.INSTANCE.isEnabled()) {
                event.setY(event.getY() * fastSwimYLava.getValue());
            }
            event.setZ(event.getZ()*fastSwimXZLava.getValue());
        }
    }

    @Override
    public void onToggle() {
        ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50);
        cooldown = 0;
        damageMultiplier = 1.0d;
        timer.reset();
    }


    private enum SkipMode {
        TICK,
        MS,
        NONE
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.0d;
        if(addSpeed.getValue()) {
            try{
                baseSpeed = Double.valueOf(speedAddition.getValue());
            } catch(Exception e) {
                baseSpeed = 0.272;
            }
        } else {
            baseSpeed = 0.272;
        }
        if(mc.player.isSneaking()) {
            baseSpeed = sneakSpeed.getValue() / 100d;
        }
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier+1));
        }
        if(useTimer.getValue()) {
            ((ITimer)((IMinecraft)mc).getTimer()).setTickLength(50 / 1.088f);
        }
        if(damageBoost.getValue()) {
            baseSpeed*=damageMultiplier;
        }
        return baseSpeed;
    }


    private boolean canSprint() {
        return ((mc.player.movementInput.moveStrafe != 0.0F || mc.player.moveForward != 0.0F) && !mc.player.isActiveItemStackBlocking() && !mc.player.isOnLadder() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6);
    }
}
