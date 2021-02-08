package me.fluffy.dactyl.module.impl.player;

import io.netty.util.internal.ConcurrentSet;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.event.impl.world.SetOpaqueCubeEvent;
import me.fluffy.dactyl.injection.inj.access.ISPacketPlayerPosLook;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PacketFly extends Module {
    public Setting<Mode> mode = new Setting<Mode>("Mode", Mode.FACTOR);
    public Setting<Double> tickCount = new Setting<Double>("Factor", 1d, 1d, 10d, v->mode.getValue().equals(Mode.FACTOR));
    public Setting<Integer> factorAddition = new Setting<Integer>("Addition", 0, 0, 5, v->mode.getValue().equals(Mode.FACTOR));
    public Setting<Type> typeSetting = new Setting<Type>("Type", Type.DOWN);
    public Setting<PhaseMode> phaseSetting = new Setting<PhaseMode>("Phase", PhaseMode.FULL);
    public Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", true);
    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }

    private int packetCounter = 0;
    private int currentTeleportId = 0;
    private int bypassCounter = 0;
    private final Map<Integer, TimeVec3d> posLooks = new ConcurrentHashMap<Integer, TimeVec3d>();
    private final Set<CPacketPlayer> playerPackets = new ConcurrentSet<>();

    @Override
    public void onClientUpdate() {
        posLooks.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getTime() > TimeUnit.SECONDS.toMillis(30L));
    }

    @SubscribeEvent
    public void onUpdateWalking(EventUpdateWalkingPlayer event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(bypassCounter >= 10) {
            bypassCounter = 0;
        }
        this.setModuleInfo(mode.getValue().toString());
        double ySpeed;
        if(event.getStage() != ForgeEvent.Stage.PRE) {
            return;
        }
        mc.player.motionZ = 0.0;
        mc.player.motionY = 0.0;
        mc.player.motionX = 0.0;
        if (!(mode.getValue().equals(Mode.SETBACK)) && this.currentTeleportId == 0) {
            if (iteratePacketCounterCheckMaxPackets(4)) {
                sendPackets(0.0, 0.0, 0.0, false);
            }
            return;
        }
        boolean isPhasing = isPlayerCollisionBoundingBoxEmpty();
        if (mc.player.movementInput.jump && (isPhasing || !playerMoveForwardOrStrafing())) {
            double d3;
            if (antiKick.getValue() && !isPhasing) {
                d3 = iteratePacketCounterCheckMaxPackets((mode.getValue().equals(Mode.SETBACK)) ? 10 : 20) ? -0.032 : 0.062;
            } else {
                d3 = 0.062;
            }
            ySpeed = d3;
        } else if (mc.player.movementInput.sneak) {
            ySpeed = -0.062;
        } else {
            ySpeed = !isPhasing ? (iteratePacketCounterCheckMaxPackets(4) ? ((antiKick.getValue()) ? -0.04 : 0.0) : 0.0) : 0.0;
        }
        if (phaseSetting.getValue().equals(PhaseMode.FULL) && isPhasing && playerMoveForwardOrStrafing() && ySpeed != 0.0) {
            ySpeed /= 2.5;
        }
        double[] dirSpeed = getSpeed(phaseSetting.getValue().equals(PhaseMode.FULL) && isPhasing ? 0.031 : 0.26);
        bypassCounter++;
        if(mode.getValue().equals(Mode.FACTOR)) {
            BigDecimal bigDecimal = BigDecimal.valueOf(tickCount.getValue());
            int intPart = bigDecimal.intValue();
            int timesTicks = (int) ((tickCount.getValue()-intPart)*10);
            int factorTicks = (bypassCounter <= timesTicks ? ((int)Math.ceil(tickCount.getValue().doubleValue())) : ((int)(tickCount.getValue().doubleValue())));
            for (int i = 1; i <= factorTicks+factorAddition.getValue(); ++i) {
                mc.player.motionX = dirSpeed[0] * (double) i;
                mc.player.motionY = ySpeed * (double) i;
                mc.player.motionZ = dirSpeed[1] * (double) i;
                sendPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ, !mode.getValue().equals(Mode.SETBACK));
            }
        } else {
            for (int i = 1; i <= 1; ++i) {
                mc.player.motionX = dirSpeed[0] * (double) i;
                mc.player.motionY = ySpeed * (double) i;
                mc.player.motionZ = dirSpeed[1] * (double) i;
                sendPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ, !mode.getValue().equals(Mode.SETBACK));
            }
        }
    }

    @Override
    public void onEnable() {
        clearValues();
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (!(mode.getValue().equals(Mode.SETBACK)) && this.currentTeleportId == 0) {
            return;
        }
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);
        if (!(phaseSetting.getValue().equals(PhaseMode.OFF)) && (phaseSetting.getValue().equals(PhaseMode.SEMI) || isPlayerCollisionBoundingBoxEmpty())) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if (event.getPacket() instanceof CPacketPlayer) {
                CPacketPlayer cPacketPlayer = (CPacketPlayer)event.getPacket();
                if (playerPackets.contains(cPacketPlayer)) {
                    playerPackets.remove(cPacketPlayer);
                    return;
                }
                event.setCanceled(true);
            }
        } else {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook sPacketPlayerPosLook = (SPacketPlayerPosLook)event.getPacket();
                if (mc.player.isEntityAlive()) {
                    TimeVec3d posVec;
                    if (mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ), false) && !(mc.currentScreen instanceof GuiDownloadTerrain) && !(mode.getValue().equals(Mode.SETBACK)) && (posVec = (TimeVec3d) posLooks.remove(sPacketPlayerPosLook.getTeleportId())) != null && posVec.x == sPacketPlayerPosLook.getX() && posVec.y == sPacketPlayerPosLook.getY() && posVec.z == sPacketPlayerPosLook.getZ()) {
                        event.setCanceled(true);
                        return;
                    }
                }
                ((ISPacketPlayerPosLook)sPacketPlayerPosLook).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)sPacketPlayerPosLook).setPitch(mc.player.rotationPitch);
                this.currentTeleportId = sPacketPlayerPosLook.getTeleportId();
            }
        }
    }

    @SubscribeEvent
    public void onOpaque(SetOpaqueCubeEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPush(PlayerSPPushOutOfBlocksEvent event) {
        event.setCanceled(true);
    }

    private boolean playerMoveForwardOrStrafing() {
        return ((mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0));
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


    private void clearValues() {
        this.packetCounter = 0;
        this.bypassCounter = 0;
        this.currentTeleportId = 0;
        this.playerPackets.clear();
        this.posLooks.clear();
    }

    private boolean isPlayerCollisionBoundingBoxEmpty() {
        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(0.0, 0.0, 0.0)).isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean iteratePacketCounterCheckMaxPackets(int n) {
        if (++this.packetCounter >= n) {
            this.packetCounter = 0;
            return true;
        }
        return false;
    }

    private void sendPackets(double d2, double d3, double d4, boolean b1) {
        Vec3d vec3d = new Vec3d(d2, d3, d4);
        Vec3d vec3d2 = mc.player.getPositionVector().add(vec3d);
        Vec3d vec3d3 = outOfBoundsVec(vec3d, vec3d2);
        this.sendPlayerPacket(new CPacketPlayer.Position(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.onGround));
        this.sendPlayerPacket(new CPacketPlayer.Position(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.onGround));
        if (b1) {
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(++this.currentTeleportId));
            this.posLooks.put(this.currentTeleportId, new TimeVec3d(vec3d2.x, vec3d2.y, vec3d2.z, System.currentTimeMillis()));
        }
    }

    private Vec3d outOfBoundsVec(final Vec3d offset, final Vec3d position) {
        return position.add(offset.x,  FlyingType.getOffset(typeSetting.getValue()), offset.z);
    }


    private void sendPlayerPacket(CPacketPlayer cPacketPlayer) {
        this.playerPackets.add(cPacketPlayer);
        mc.player.connection.sendPacket(cPacketPlayer);
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

    public class TimeVec3d extends Vec3d {
        private final long time;

        public TimeVec3d(double xIn, double yIn, double zIn, long time) {
            super(xIn, yIn, zIn);
            this.time = time;
        }

        public TimeVec3d(Vec3i vector, long time) {
            super(vector);
            this.time = time;
        }

        public long getTime() {
            return time;
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
