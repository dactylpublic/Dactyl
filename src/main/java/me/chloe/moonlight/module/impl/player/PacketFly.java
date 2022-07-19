package me.chloe.moonlight.module.impl.player;

import io.netty.util.internal.ConcurrentSet;
import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.event.ForgeEvent;
import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.event.impl.player.EventUpdateWalkingPlayer;
import me.chloe.moonlight.event.impl.player.MoveEvent;
import me.chloe.moonlight.util.MathUtil;
import me.chloe.moonlight.event.impl.world.SetOpaqueCubeEvent;
import me.chloe.moonlight.injection.inj.access.ISPacketPlayerPosLook;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.module.impl.client.Colors;
import me.chloe.moonlight.setting.Setting;
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

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PacketFly extends Module {
    public Setting<Mode> mode = new Setting<Mode>("Mode", Mode.FACTOR);
    public Setting<Float> tickCountFly = new Setting<Float>("Factor", 1f, 0.5f, 10f, v->mode.getValue().equals(Mode.FACTOR));
    public Setting<Type> typeSetting = new Setting<Type>("Type", Type.DOWN);
    public Setting<PhaseMode> phaseSetting = new Setting<PhaseMode>("Phase", PhaseMode.FULL);
	public Setting<Boolean> delayConfirmTeleport = new Setting<Boolean>("Frequency", false);
    public Setting<Integer> antiSetbackSpam = new Setting<Integer>("Limit", 0, 0, 10);
    public Setting<Boolean> debugMode = new Setting<Boolean>("Debug", false);
    public Setting<Boolean> antiKick = new Setting<Boolean>("AntiKick", true);
    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }

    private int packetCounter = 0;
    private int currentTeleportId = 0;
    private int bypassCounter = 0;
    private int frequencyTick = 0;
    private float lastYaw, lastPitch;

    private float debugVal1 = 0f;
    private float debugVal2 = 0f;
    private float debugVal3 = 0f;

    private final Map<Integer, TimeVec3d> posLooks = new ConcurrentHashMap<Integer, TimeVec3d>();
    private final Set<CPacketPlayer> playerPackets = new ConcurrentSet<>();

    private CPacketConfirmTeleport delayedPacket = null;

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
            float rawFactor = tickCountFly.getValue();
            int factorInt = (int) Math.floor(rawFactor);
            float extraFactor = rawFactor - (float) factorInt;
            if (Math.random() <= extraFactor) {
                factorInt++;
            }
            for (int i = 1; i <= factorInt; ++i) {
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

    @Override
    public void onScreen() {
        if(mc.player == null) {
            return;
        }
        if(debugMode.getValue()) {
            Moonlight.fontUtil.drawStringWithShadow("Current Packet Yaw: " + String.valueOf(debugVal1), 1, 30, Colors.INSTANCE.getColor(30, false));
            Moonlight.fontUtil.drawStringWithShadow("Current Packet Yaw Divided: " + String.valueOf(debugVal2), 1, 40, Colors.INSTANCE.getColor(40, false));
            Moonlight.fontUtil.drawStringWithShadow("Last Packet Yaw: " + String.valueOf(debugVal3), 1, 50, Colors.INSTANCE.getColor(50, false));
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
                    float dividedYaw = MathUtil.roundFloat(sPacketPlayerPosLook.getYaw() / 2, 2);
                    float lastValidYaw = MathUtil.roundFloat(lastYaw, 2);

                    debugVal1 = MathUtil.roundFloat(sPacketPlayerPosLook.getYaw(), 2);
                    debugVal2 = dividedYaw;
                    debugVal3 = lastValidYaw;

                    boolean isNormalPacket = (mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ), false) && !(mc.currentScreen instanceof GuiDownloadTerrain) && !(mode.getValue().equals(Mode.SETBACK)) && (posVec = (TimeVec3d) posLooks.remove(sPacketPlayerPosLook.getTeleportId())) != null && posVec.x == sPacketPlayerPosLook.getX() && posVec.y == sPacketPlayerPosLook.getY() && posVec.z == sPacketPlayerPosLook.getZ());
                    // lol nice pay to win patch 0x22 and bullet
                    // fluffy u should quit drugs ngl
                    /*frequencyTick++;
                    boolean isFrequencyPacket = (frequency.getValue() && sPacketPlayerPosLook.getYaw() == 0.0f);
                    if(isFrequencyPacket && !((posVec = (TimeVec3d) posLooks.remove(sPacketPlayerPosLook.getTeleportId())) != null && posVec.x == sPacketPlayerPosLook.getX() && posVec.y == sPacketPlayerPosLook.getY() && posVec.z == sPacketPlayerPosLook.getZ())) {
                        event.setCanceled(true);
                        return;
                    }*/
                    if (isNormalPacket) {
                        event.setCanceled(true);
                        return;
                    }
                }
                ((ISPacketPlayerPosLook)sPacketPlayerPosLook).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)sPacketPlayerPosLook).setPitch(mc.player.rotationPitch);
                this.currentTeleportId = sPacketPlayerPosLook.getTeleportId();
                this.lastYaw = sPacketPlayerPosLook.getYaw();
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
        this.frequencyTick = 0;
        this.playerPackets.clear();
        this.posLooks.clear();
        this.delayedPacket = null;
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
        if (delayedPacket != null) {
            mc.player.connection.sendPacket(delayedPacket);
            delayedPacket = null;
        }
        Vec3d vec3d = new Vec3d(d2, d3, d4);
        Vec3d vec3d2 = mc.player.getPositionVector().add(vec3d);
        Vec3d vec3d3 = outOfBoundsVec(vec3d, vec3d2);
        this.sendPlayerPacket(new CPacketPlayer.Position(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.onGround));
        this.sendPlayerPacket(new CPacketPlayer.Position(vec3d3.x, vec3d3.y, vec3d3.z, mc.player.onGround));
        for (int i = 0; i < antiSetbackSpam.getValue(); i++) {
            this.sendPlayerPacket(new CPacketPlayer.Position(vec3d2.x, vec3d2.y, vec3d2.z, mc.player.onGround));
        }
        if (b1) {
            currentTeleportId++;
            this.posLooks.put(this.currentTeleportId, new TimeVec3d(vec3d2.x, vec3d2.y, vec3d2.z, System.currentTimeMillis()));
            if (delayConfirmTeleport.getValue()) {
                delayedPacket = new CPacketConfirmTeleport(this.currentTeleportId);
            } else {
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.currentTeleportId));
            }
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
                case LIMITJITTER:
                    int j = rand.nextInt(22) + 70;
                    if(rand.nextBoolean()) {
                        return j;
                    }
                    return -j;
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
        UP("Up"),
        DOWN("Down"),
        PRESERVE("Preserve"),
        LIMITJITTER("LimitJitter");
        private final String name;
        private Type(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum PhaseMode {
        FULL,
        SEMI,
        OFF
    }
}
