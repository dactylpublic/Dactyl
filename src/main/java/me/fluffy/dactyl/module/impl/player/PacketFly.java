package me.fluffy.dactyl.module.impl.player;

import io.netty.util.internal.ConcurrentSet;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.event.impl.world.BlockPushEvent;
import me.fluffy.dactyl.event.impl.world.SetOpaqueCubeEvent;
import me.fluffy.dactyl.injection.inj.access.ISPacketPlayerPosLook;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketFly extends Module {
    public Setting<Mode> mode = new Setting<Mode>("Mode", Mode.FACTOR);
    public Setting<Integer> tickCount = new Setting<Integer>("Factor", 1, 1, 10);
    public Setting<Type> typeSetting = new Setting<Type>("Type", Type.DOWN);
    public Setting<PhaseMode> phaseSetting = new Setting<PhaseMode>("Phase", PhaseMode.FULL);
    public Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", true);
    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }
    private int teleportId;
    private int flightCounter = 0;
    private final Set<CPacketPlayer> packets = new ConcurrentSet();
    private final Map<Integer, IDTime> idTimeMap = new ConcurrentHashMap<Integer, IDTime>();

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        double d = 0.0d;
        if (event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        this.setModuleInfo(mode.getValue() == Mode.FACTOR ? "Factor" : "Fast");
        mc.player.motionZ = 0.0;
        mc.player.motionY = 0.0;
        mc.player.motionX = 0.0;
        if (mode.getValue() != Mode.FAST && flightCounter == 0) {
            if (idHasPassed(4)) {
                doPackets(0.0, 0.0, 0.0);
            }
            return;
        }
        boolean bl = canCollide();
        if (mc.player.movementInput.jump && (bl || !EntityUtil.isMoving())) {
            d = canCollide() && !bl ? (idHasPassed(mode.getValue() == Mode.FACTOR ? 10 : 20) ? -0.032 : 0.062) : 0.062;
        } else if (mc.player.movementInput.sneak) {
            d = -0.062;
        } else {
            d = !bl ? (idHasPassed(4) ? (antiKick.getValue() ? -0.04 : 0.0) : 0.0) : (d = 0.0);
        }
        if (phaseSetting.getValue() == PhaseMode.FULL && bl && EntityUtil.isMoving() && d != 0.0) {
            d /= 2.5;
        }
        double[] arrd = getSpeed(phaseSetting.getValue() == PhaseMode.FULL && bl ? 0.031 : 0.26);
        for (int i = 1; i <= (phaseSetting.getValue() == PhaseMode.FULL ? tickCount.getValue() : 1); ++i) {
            mc.player.motionX = arrd[0] * (double)i;
            mc.player.motionY = d * (double)i;
            mc.player.motionZ = arrd[1] * (double)i;
            doPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(mode.getValue() != Mode.FAST && flightCounter == 0) {
            return;
        }
        event.setMotion(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
        if(phaseSetting.getValue() == PhaseMode.SEMI && canCollide()) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayer) {
                if(packets.contains(event.getPacket())) {
                    packets.remove(event.getPacket());
                    return;
                }
                event.setCanceled(true);
            }
        } else {
            if(event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook p = (SPacketPlayerPosLook)event.getPacket();
                if(!mc.player.isEntityAlive() || !mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ), false) || mc.currentScreen instanceof GuiDownloadTerrain || mode.getValue() == Mode.FACTOR || idTimeMap.remove(p.getTeleportId()) != null) {

                }
                ((ISPacketPlayerPosLook)p).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)p).setPitch(mc.player.rotationPitch);
                flightCounter = p.getTeleportId();
            }
        }
    }

    @SubscribeEvent
    public void onOpaque(SetOpaqueCubeEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPush(BlockPushEvent event) {
        event.setCanceled(true);
    }

    public static double[] getSpeed(double d) {
        double d2;
        double d3;
        MovementInput movementInput = mc.player.movementInput;
        double d4 = movementInput.moveForward;
        double d5 = movementInput.moveStrafe;
        float f = mc.player.rotationYaw;
        if (d4 == 0.0 && d5 == 0.0) {
            d2 = d3 = 0.0;
        } else {
            if (d4 != 0.0) {
                if (d5 > 0.0) {
                    f += (float)(d4 > 0.0 ? -45 : 45);
                } else if (d5 < 0.0) {
                    f += (float)(d4 > 0.0 ? 45 : -45);
                }
                d5 = 0.0;
                if (d4 > 0.0) {
                    d4 = 1.0;
                } else if (d4 < 0.0) {
                    d4 = -1.0;
                }
            }
            d3 = d4 * d * Math.cos(Math.toRadians(f + 90.0f)) + d5 * d * Math.sin(Math.toRadians(f + 90.0f));
            d2 = d4 * d * Math.sin(Math.toRadians(f + 90.0f)) - d5 * d * Math.cos(Math.toRadians(f + 90.0f));
        }
        return new double[]{d3, d2};
    }

    private void doPackets(double d, double d2, double d3) {
        Vec3d vec3d = new Vec3d(d, d2, d3);
        Vec3d vec3d2 = mc.player.getPositionVector().add(vec3d);
        double addition = (typeSetting.getValue() == Type.DOWN ? -1337 : 1337);
        Vec3d vec3d3 = vec3d.add(0, addition, 0);
        add(new CPacketPlayer.Position(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.onGround));
        add(new CPacketPlayer.Position(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.onGround));
    }

    private void add(CPacketPlayer cPacketPlayer) {
        packets.add(cPacketPlayer);
        mc.player.connection.sendPacket(cPacketPlayer);
    }

    private boolean canCollide() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
    }

    private boolean idHasPassed(int id) {
        ++teleportId;
        if(teleportId >= id) {
            teleportId = 0;
            return true;
        }
        return false;
    }


    public static class IDTime {
        private final Vec3d pos;
        private final TimeUtil timer;

        public IDTime(final Vec3d pos, final TimeUtil timer) {
            this.pos = pos;
            (this.timer = timer).reset();
        }

        public Vec3d getPos() {
            return this.pos;
        }

        public TimeUtil getTimer() {
            return this.timer;
        }
    }

    public enum Mode {
        FAST,
        FACTOR
    }

    public enum Type {
        UP,
        DOWN
    }

    public enum PhaseMode {
        FULL,
        SEMI
    }
}
