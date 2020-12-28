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
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketFly extends Module {
    public Setting<Mode> mode = new Setting<Mode>("Mode", Mode.FACTOR);
    public Setting<Integer> tickCount = new Setting<Integer>("Factor", 1, 1, 10, v->mode.getValue().equals(Mode.FACTOR));
    public Setting<Type> typeSetting = new Setting<Type>("Type", Type.DOWN);
    public Setting<PhaseMode> phaseSetting = new Setting<PhaseMode>("Phase", PhaseMode.FULL);
    public Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", true);
    public Setting<Boolean> cancelSPacket = new Setting<Boolean>("TCancel", true);
    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }
    private int teleportId;
    private int flightCounter;
    private final Set<CPacketPlayer> packetSet = new ConcurrentSet();
    private final Map<Integer, IDTime> idTimeMap = new ConcurrentHashMap<Integer, IDTime>();

    @Override
    public void onClientUpdate() {
        this.idTimeMap.entrySet().removeIf(idTime -> idTime.getValue().getTimer().hasPassed(30L));
    }

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if(event.getStage() == ForgeEvent.Stage.POST) {
            return;
        }
        double d = 0.0;
        this.setModuleInfo(mode.getValue().toString());
        mc.player.motionX = 0.0d;
        mc.player.motionY = 0.0d;
        mc.player.motionZ = 0.0d;
        if (mode.getValue() != Mode.SETBACK && teleportId == 0) {
            if (checkCounter(4)) {
                this.doPackets(0, 0, 0, false);
            }
            return;
        }
        boolean b1 = canCollide();
        if(mc.player.movementInput.jump && (b1 || !(mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0))) {
            d = antiKick.getValue() && !b1 ? (checkCounter((mode.getValue() == Mode.SETBACK) ? 10 : 20) ? -0.032 : 0.062) : 0.062;
        } else if(mc.player.movementInput.sneak) {
            d = -0.062;
        } else {
            double d2 = !b1 ? (checkCounter(4) ? (antiKick.getValue() ? -0.04 : 0.0) : 0.0) : (d = 0.0);
        }
        if (phaseSetting.getValue() == PhaseMode.FULL && b1 && (mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0) && d != 0.0) {
            d /= 2.5;
        }
        double[] arrd = getSpeed(phaseSetting.getValue().equals(PhaseMode.FULL) && b1 ? 0.031 : 0.26);
        for (int i = 1; i <= (mode.getValue() == Mode.FACTOR ? tickCount.getValue() : 1); ++i) {
            mc.player.motionX = arrd[0] * (double)i;
            mc.player.motionY = d * (double)i;
            mc.player.motionZ = arrd[1] * (double)i;
            doPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ, !mode.getValue().equals(Mode.SETBACK));
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(!mode.getValue().equals(Mode.SETBACK) && teleportId == 0) {
            return;
        }
        event.setMotion(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
        if(!phaseSetting.getValue().equals(PhaseMode.OFF) && phaseSetting.getValue().equals(PhaseMode.SEMI) && canCollide()) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayer) {
                CPacketPlayer p = (CPacketPlayer)event.getPacket();
                if(packetSet.contains(p)) {
                    packetSet.remove(p);
                    return;
                }
                event.setCanceled(true);
            }
        } else {
            if (event.getPacket() instanceof SPacketPlayerPosLook && !(mc.player == null || mc.world == null)) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.getPacket();
                if (mc.player.isEntityAlive()) {
                    final BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                    if (mc.world.isBlockLoaded(pos, false) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
                        if(idTimeMap.containsKey(packet.getTeleportId())) {
                            idTimeMap.remove(packet.getTeleportId());
                            if(cancelSPacket.getValue()) {
                                event.setCanceled(true);
                                return;
                            }
                        }
                    }
                }
                ((ISPacketPlayerPosLook)packet).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)packet).setPitch(mc.player.rotationPitch);
                this.teleportId = packet.getTeleportId();
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

    @Override
    public void onToggle() {
        clean();
    }


    public void add(CPacketPlayer cPacketPlayer) {
        packetSet.add(cPacketPlayer);
        mc.player.connection.sendPacket(cPacketPlayer);
    }

    public boolean checkCounter(int n) {
        if(++flightCounter >= n) {
            flightCounter = 0;
            return true;
        }
        return false;
    }

    public void doPackets(double d, double d2, double d3, boolean teleport) {
        Vec3d vec3d = new Vec3d(d, d2, d3);
        Vec3d vec3d2 = mc.player.getPositionVector().add(vec3d);
        Vec3d vec3d3 = this.outOfBoundsVec(vec3d, vec3d2);
        this.add(new CPacketPlayer.Position(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.onGround));
        this.add(new CPacketPlayer.Position(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.onGround));
        if(teleport) {
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(++this.teleportId));
            idTimeMap.put(this.teleportId, new IDTime(vec3d2, new TimeUtil()));
        }
    }

    private Vec3d outOfBoundsVec(final Vec3d offset, final Vec3d position) {
        return position.add(0.0,  FlyingType.getOffset(typeSetting.getValue()), 0.0);
    }


    public void clean() {
        flightCounter = 0;
        teleportId = 0;
        packetSet.clear();
        idTimeMap.clear();
    }

    public static class FlyingType {
        private static final Random rand = new Random();
        public static double getOffset(Type type) {
            switch(type) {
                case UP:
                    return 1337.0D;
                case DOWN:
                    return -1337.0D;
                case PRESERVE:
                    int n = rand.nextInt(29000000);
                    if(rand.nextBoolean()) {
                        return n;
                    }
                    return -n;
            }
            return 0.0D;
        }
    }

    private boolean canCollide() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
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
        SETBACK("Setback"),
        FAST("Fast"),
        FACTOR("Factor");
        private final String name;
        private Mode(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum Type {
        UP,
        DOWN,
        PRESERVE
    }

    public enum PhaseMode {
        FULL,
        SEMI,
        OFF
    }
}
