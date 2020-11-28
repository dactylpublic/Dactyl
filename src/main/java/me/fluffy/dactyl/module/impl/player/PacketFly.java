package me.fluffy.dactyl.module.impl.player;

import io.netty.util.internal.ConcurrentSet;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.event.impl.world.BlockPushEvent;
import me.fluffy.dactyl.injection.inj.access.ISPacketPlayerPosLook;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author 3arthqu4ke
 * narrowed down phobos packetfly (credit)
 */

public class PacketFly extends Module {

    public boolean flight = true;
    public int flightMode = 0;
    public boolean doAntiFactor = true;
    public double antiFactor = 2.5d;
    public double extraFactor = 1d;
    public boolean strafeFactor = true;
    public int loops = 1;
    public boolean clearTeleMap = true;
    public int mapTime = 30;
    public boolean clearIDs = true;
    public boolean setYaw = true;
    public boolean setID = true;
    public boolean setMove = false;
    public boolean nocliperino = false;
    public boolean sendTeleport = true;
    public boolean resetID = true;
    public boolean setPos = false;
    public boolean invalidPacket = true;

    private final Set<CPacketPlayer> packets = (Set<CPacketPlayer>)new ConcurrentSet();
    private final Map<Integer, IDtime> teleportmap = new ConcurrentHashMap<Integer, IDtime>();

    private int flightCounter = 0;
    private int teleportID = 0;


    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }

    @Override
    public void onClientUpdate() {
        this.teleportmap.entrySet().removeIf(idTime -> this.clearTeleMap && idTime.getValue().getTimer().hasPassed(this.mapTime));
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(EventUpdateWalkingPlayer event) {
        if (event.getStage() == ForgeEvent.Stage.POST) {
            return;
        }
        this.setModuleInfo("Fast");
        mc.player.setVelocity(0.0, 0.0, 0.0);
        double speed = 0.0;
        final boolean checkCollisionBoxes = this.checkHitBoxes();
        if (mc.player.movementInput.jump && (checkCollisionBoxes || !(mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0))) {
            if (this.flight && !checkCollisionBoxes) {
                if (this.flightMode == 0) {
                    speed = (this.resetCounter(10) ? -0.032 : 0.062);
                }
                else {
                    speed = (this.resetCounter(20) ? -0.032 : 0.062);
                }
            }
            else {
                speed = 0.062;
            }
        }
        else if (mc.player.movementInput.sneak) {
            speed = -0.062;
        }
        else if (!checkCollisionBoxes) {
            speed = (this.resetCounter(4) ? (this.flight ? -0.04 : 0.0) : 0.0);
        }
        else {
            speed = 0.0;
        }
        if (this.doAntiFactor && checkCollisionBoxes && (mc.player.moveForward != 0.0 || mc.player.moveStrafing != 0.0) && speed != 0.0) {
            speed /= this.antiFactor;
        }
        final double[] strafing = this.getMotion((this.strafeFactor && checkCollisionBoxes) ? 0.031 : 0.26);
        for (int i = 1; i < this.loops + 1; ++i) {
            mc.player.motionX = strafing[0] * i * this.extraFactor;
            mc.player.motionY = speed * i;
            mc.player.motionZ = strafing[1] * i * this.extraFactor;
            this.sendPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ, this.sendTeleport);
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (this.setMove && this.flightCounter != 0) {
            event.setX(mc.player.motionX);
            event.setY(mc.player.motionY);
            event.setZ(mc.player.motionZ);
            if (this.nocliperino && this.checkHitBoxes()) {
                mc.player.noClip = true;
            }
        }
    }
    @SubscribeEvent
    public void onPacketSend(PacketEvent event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            final CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (this.packets.contains(packet)) {
                this.packets.remove(packet);
            }
            else {
                event.setCanceled(true);
            }
        }

        if (event.getPacket() instanceof SPacketPlayerPosLook && !(mc.player == null || mc.world == null)) {
            final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            if (mc.player.isEntityAlive()) {
                final BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                if (mc.world.isBlockLoaded(pos, false) && !(mc.currentScreen instanceof GuiDownloadTerrain) && this.clearIDs) {
                    this.teleportmap.remove(packet.getTeleportId());
                }
            }
            if (this.setYaw) {
                ((ISPacketPlayerPosLook)packet).setYaw(mc.player.rotationYaw);
                ((ISPacketPlayerPosLook)packet).setPitch(mc.player.rotationPitch);
            }
            if (this.setID) {
                this.teleportID = packet.getTeleportId();
            }
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(final BlockPushEvent event) {
        event.setCanceled(true);
    }


    private boolean checkHitBoxes() {
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
    }

    private boolean resetCounter(final int counter) {
        if (++this.flightCounter >= counter) {
            this.flightCounter = 0;
            return true;
        }
        return false;
    }

    private double[] getMotion(final double speed) {
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (moveForward != 0.0f) {
            if (moveStrafe > 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
            }
            else if (moveStrafe < 0.0f) {
                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0f) {
                moveForward = 1.0f;
            }
            else if (moveForward < 0.0f) {
                moveForward = -1.0f;
            }
        }
        final double posX = moveForward * speed * -Math.sin(Math.toRadians(rotationYaw)) + moveStrafe * speed * Math.cos(Math.toRadians(rotationYaw));
        final double posZ = moveForward * speed * Math.cos(Math.toRadians(rotationYaw)) - moveStrafe * speed * -Math.sin(Math.toRadians(rotationYaw));
        return new double[] { posX, posZ };
    }

    private void sendPackets(final double x, final double y, final double z, final boolean teleport) {
        final Vec3d vec = new Vec3d(x, y, z);
        final Vec3d position = mc.player.getPositionVector().add(vec);
        final Vec3d outOfBoundsVec = this.outOfBoundsVec(vec, position);
        this.packetSender((CPacketPlayer)new CPacketPlayer.Position(position.x, position.y, position.z, mc.player.onGround));
        if (this.invalidPacket) {
            this.packetSender((CPacketPlayer)new CPacketPlayer.Position(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, mc.player.onGround));
        }
        if (this.setPos) {
            mc.player.setPosition(position.x, position.y, position.z);
        }
        this.teleportPacket(position, teleport);
    }

    private void teleportPacket(final Vec3d pos, final boolean shouldTeleport) {
        if (shouldTeleport) {
            mc.player.connection.sendPacket((Packet)new CPacketConfirmTeleport(++this.teleportID));
            this.teleportmap.put(this.teleportID, new IDtime(pos, new TimeUtil()));
        }
    }

    private Vec3d outOfBoundsVec(final Vec3d offset, final Vec3d position) {
        return position.add(0.0, 1337.0, 0.0);
    }

    private void packetSender(final CPacketPlayer packet) {
        this.packets.add(packet);
        mc.player.connection.sendPacket((Packet)packet);
    }

    private void clean() {
        this.teleportmap.clear();
        this.flightCounter = 0;
        if (this.resetID) {
            this.teleportID = 0;
        }
        this.packets.clear();
    }


    public static class IDtime
    {
        private final Vec3d pos;
        private final TimeUtil timer;

        public IDtime(final Vec3d pos, final TimeUtil timer) {
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

}
