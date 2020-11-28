package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.MathUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class AltSpeed extends Module {
    public Setting<SpeedMode> modeSetting = new Setting<SpeedMode>("Mode", SpeedMode.ONGROUND);
    public AltSpeed() {
        super("AltSpeed", Category.MOVEMENT, "OnGround and more (others modes coming soon)");
    }

    public static int ticks;
    public static double moveSpeed;
    private double lastDist;
    private double save;
    public static boolean canStep;
    private boolean stop;
    public static boolean boost = false;


    @Override
    public void onClientUpdate() {
        this.setModuleInfo(modeSetting.getValue().toString());
        this.setDisplayName("Speed");
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(mc.player.isElytraFlying()) {
            return;
        }
        if(Strafe.INSTANCE.isEnabled()) {
            return;
        }
        if(modeSetting.getValue() == SpeedMode.ONGROUND) {
            if (mc.player.onGround) {
                ticks = 2;
            }
            if (MathUtil.round(mc.player.posY - (int)mc.player.posY, 3) == MathUtil.round(0.138, 3)) {
                mc.player.motionY -= 0.08;
                event.setY(event.getY()-0.09316090325960147);
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY-0.09316090325960147, mc.player.posZ, mc.player.onGround));
            }
            if (ticks == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f)) {
                ticks = 2;
                moveSpeed = 1.35 * getBaseMoveSpeed() - 0.01;
            }
            else if (ticks == 2) {
                if (stop) {
                    moveSpeed = 0.35;
                    stop = false;
                }
                moveSpeed *= 0.9;
                ticks = 3;
                if ((mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) && !mc.player.collidedHorizontally) {
                    mc.player.motionY = 0.399399995003033;
                    event.setY(0.399399995003033);
                    moveSpeed *= (boost ? 2.4 : 2.385);
                    mc.player.motionY *= -5.0;
                    boost = false;
                }
            }
            else if (ticks == 3) {
                ticks = 4;
                final double difference = 0.66 * (lastDist - getBaseMoveSpeed());
                moveSpeed = lastDist - difference;
            }
            else if (mc.world.getCollisionBoxes(mc.player, mc.player.boundingBox.offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
                ticks = 1;
            }
            moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
            final MovementInput movementInput = mc.player.movementInput;
            float forward = movementInput.moveForward;
            float strafe = movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if (forward == 0.0f && strafe == 0.0f) {
                event.setX(0.0);
                event.setZ(0.0);
            }
            else if (forward != 0.0f) {
                if (strafe >= 1.0f) {
                    yaw += ((forward > 0.0f) ? -45 : 45);
                    strafe = 0.0f;
                }
                else if (strafe <= -1.0f) {
                    yaw += ((forward > 0.0f) ? 45 : -45);
                    strafe = 0.0f;
                }
                if (forward > 0.0f) {
                    forward = 1.0f;
                }
                else if (forward < 0.0f) {
                    forward = -1.0f;
                }
            }
            final double mx = Math.cos(Math.toRadians(yaw + 90.0f));
            final double mz = Math.sin(Math.toRadians(yaw + 90.0f));
            final double motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
            final double motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
            event.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
            event.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);
            canStep = true;
            mc.player.stepHeight = 0.6f;
            if (forward == 0.0f && strafe == 0.0f) {
                event.setX(0.0);
                event.setZ(0.0);
            }
            else {
                boolean collideCheck = false;
                if (mc.world.getCollisionBoxes(mc.player, mc.player.boundingBox.expand(0.5, 0.0, 0.5)).size() > 0) {
                    collideCheck = true;
                }
                if (forward != 0.0f) {
                    if (strafe >= 1.0f) {
                        yaw += ((forward > 0.0f) ? -45 : 45);
                        strafe = 0.0f;
                    } else if (strafe <= -1.0f) {
                        yaw += ((forward > 0.0f) ? 45 : -45);
                        strafe = 0.0f;
                    }
                    if (forward > 0.0f) {
                        forward = 1.0f;
                    } else if (forward < 0.0f) {
                        forward = -1.0f;
                    }
                }
            }
        }
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * (amplifier+1));
        }
        return baseSpeed;
    }




    public enum SpeedMode {
        ONGROUND("OnGround");

        private final String name;
        private SpeedMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
