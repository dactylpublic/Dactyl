package me.fluffy.dactyl.module.impl.movement;

import io.netty.buffer.Unpooled;
import me.fluffy.dactyl.event.impl.action.BoatMoveEvent;
import me.fluffy.dactyl.event.impl.network.NetworkExceptionEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoatFly extends Module {
    private final Setting<Double> speed = new Setting<Double>("Speed", 0.9D, 0.01D, 12.0D);
    private final Setting<Double> fallSpeed = new Setting<Double>("Fall Speed", 0d, 0d, 2d);
    private final Setting<Boolean> bypass = new Setting<Boolean>("Bypass", true);
    private final Setting<Integer> interval = new Setting<Integer>("Bypass Interval", 2, 1, 10, v->bypass.getValue());

    public BoatFly() {
        super("BoatFly", Category.MOVEMENT);
    }

    private final List<CPacketVehicleMove> packets = new ArrayList<>();
    private EntityBoat boat = null;
    private int tpId = 0;

    @SubscribeEvent
    public void onBoat(BoatMoveEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }

        if(boat == null || boat != event.getBoat()) boat = event.getBoat();

        event.y = 0;

        boat.rotationYaw = mc.player.rotationYaw;

        boat.noClip = false;

        if(!mc.gameSettings.keyBindForward.isKeyDown() &&
                !mc.gameSettings.keyBindBack.isKeyDown() &&
                !mc.gameSettings.keyBindLeft.isKeyDown() &&
                !mc.gameSettings.keyBindRight.isKeyDown()) event.x = event.z = 0;
        else {
            float yaw = mc.player.rotationYaw;
            float forward = 1;

            if(mc.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(mc.player.moveForward > 0) forward = 0.5f;

            if(mc.player.moveStrafing > 0) yaw -= 90 * forward;
            if(mc.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);

            event.x = -Math.sin(yaw) * speed.getValue();
            event.z = Math.cos(yaw) * speed.getValue();
        }

        if(mc.gameSettings.keyBindSprint.isKeyDown()) event.y = -speed.getValue();
        else if(mc.gameSettings.keyBindJump.isKeyDown()) event.y += speed.getValue();
        else event.y = -fallSpeed.getValue();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(bypass.getValue() && mc.player != null && mc.player.isRiding()) {
                if(event.getPacket() instanceof SPacketPlayerPosLook) {
                    tpId = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();
                    event.setCanceled(true);
                }
                if(event.getPacket() instanceof SPacketMoveVehicle) event.setCanceled(true);
            }
        } else {
            if(bypass.getValue() && mc.player != null && mc.player.isRiding() && boat != null) {
                if(event.getPacket() instanceof CPacketVehicleMove && mc.player.ticksExisted % interval.getValue() == 0) {
                    if(packets.contains(event.getPacket())) packets.remove(event.getPacket());
                    else {
                        try {
                            event.getPacket().readPacketData(createPacketData(boat.prevPosX, boat.prevPosY, boat.prevPosZ, boat.prevRotationYaw, boat.prevRotationPitch));
                        } catch(IOException e) {
                            ChatUtil.printMsg("BoatFly error...", true, false);
                            if(this.isEnabled()) this.toggle();
                        }
                        mc.player.connection.sendPacket(new CPacketUseEntity(boat, EnumHand.OFF_HAND));
                        mc.player.connection.sendPacket(add(new CPacketVehicleMove(boat)));
                        Vec3d pos = getRidingPosition();
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(pos.x, pos.y, pos.z, mc.player.onGround));
                        ++tpId;
                        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
                    }
                }

                if(event.getPacket() instanceof CPacketInput || event.getPacket() instanceof CPacketPlayer.Rotation || event.getPacket() instanceof CPacketSteerBoat) event.setCanceled(true);
            }
        }
    }

    private Vec3d getRidingPosition() {
        float f = 0.0F;
        float f1 = (float)((boat.isDead ? 0.009999999776482582D : boat.getMountedYOffset()) + mc.player.getYOffset());

        if(boat.getPassengers().size() > 1) {
            if(boat.getPassengers().indexOf(mc.player) == 0) f = 0.2F;
            else f = -0.6F;
        }

        Vec3d vector = (new Vec3d(f, 0.0D, 0.0D)).rotateYaw(-boat.rotationYaw * 0.017453292F - ((float)Math.PI / 2F));
        return new Vec3d(boat.posX + vector.x, boat.posY + (double)f1, boat.posZ + vector.z);
    }

    @Override
    public void onDisable() {
        if(boat != null) boat.noClip = false;
        boat = null;
        packets.clear();
    }

    @SubscribeEvent
    public void onException(NetworkExceptionEvent event) {
        event.setCanceled(true);
    }

    private CPacketVehicleMove createPacket(double x, double y, double z, float yaw, float pitch) {
        CPacketVehicleMove packet = new CPacketVehicleMove();

        try {
            packet.readPacketData(createPacketData(x, y, z, yaw, pitch));
        } catch(IOException e) {
            ChatUtil.printMsg("BoatFly error...", true, false);
            if(this.isEnabled()) this.toggle();
        }

        return packet;
    }

    private PacketBuffer createPacketData(double x, double y, double z, float yaw, float pitch) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);

        return buffer;
    }

    private <T extends CPacketVehicleMove> T add(T packet) {
        packets.add(packet);
        return packet;
    }
}
