package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.event.impl.world.BlockPushEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.List;

public class PacketFly extends Module {
    enum Mode { SETBACK, FAST }
    enum Bounds { DOWN, GROUND, UP }
    private final Setting<Mode> mode = new Setting<Mode>("Mode", Mode.SETBACK);
    private final Setting<Bounds> bounds = new Setting<Bounds>("Bounds", Bounds.DOWN);
    private final Setting<Double> speed = new Setting<Double>("Speed", 0.2, 0.01, 0.5);
    private final Setting<Double> upSpeed = new Setting<Double>("UpSpeed", 0.05, 0.01, 0.2);
    private final Setting<Boolean> noPacketKick = new Setting<Boolean>("No Kick", true);
    private final Setting<Boolean> smooth = new Setting<Boolean>("Smooth", true);
    private final Setting<Boolean> extraPacket = new Setting<Boolean>("Extra Packets", true);

    private final List<CPacketPlayer> packets = new ArrayList<>();
    private double serverX = 0;
    private double serverY = 0;
    private double serverZ = 0;
    private int tpId = 0;
    private float pitch = 0;
    private float yaw = 0;


    public PacketFly() {
        super("PacketFly", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        pitch = mc.player.rotationPitch;
        yaw = mc.player.rotationYaw;

        serverX = mc.player.posX;
        serverY = mc.player.posY;
        serverZ = mc.player.posZ;
    }

    @Override
    public void onDisable() {
        tpId = 0;
        packets.clear();
    }

    private double getFallSpeed() {
        return mode.getValue() == Mode.SETBACK ? 0.003 : 0.03;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        boolean keyPressed = mc.gameSettings.keyBindForward.isKeyDown() ||
                mc.gameSettings.keyBindBack.isKeyDown() ||
                mc.gameSettings.keyBindLeft.isKeyDown() ||
                mc.gameSettings.keyBindRight.isKeyDown();

        // calculate movement velocities
        double x = mc.player.posX;
        double y = mc.player.posY + (mc.gameSettings.keyBindJump.isKeyDown() ? upSpeed.getValue() : (!keyPressed ? 0 : -getFallSpeed()));
        double z = mc.player.posZ;

        if(keyPressed) {
            float yaw = mc.player.rotationYaw;
            float forward = 1;

            if(mc.player.moveForward < 0) {
                yaw += 180;
                forward = -0.5f;
            } else if(mc.player.moveForward > 0) forward = 0.5f;

            if(mc.player.moveStrafing > 0) yaw -= 90 * forward;
            if(mc.player.moveStrafing < 0) yaw += 90 * forward;

            yaw = (float) Math.toRadians(yaw);
            x += -Math.sin(yaw) * speed.getValue();
            z += Math.cos(yaw) * speed.getValue();
        }

        // calculate rotation
        float yaw = smooth.getValue() ? this.yaw : mc.player.rotationYaw;
        float pitch = smooth.getValue() ? this.pitch : mc.player.rotationPitch;

        // send move packets and keep player and the position the server thinks its at
        if(mode.getValue() == Mode.SETBACK) setbackMove(x, y, z, yaw, pitch);
        else fastMove(x, y, z, yaw, pitch);
        mc.player.setPosition(serverX, serverY, serverZ);
        mc.player.setVelocity(0, 0, 0);
        event.setMotion(0, 0, 0);
    }

    private void fastMove(double x, double y, double z, float yaw, float pitch) {
        // send new position and confirm with predicted id
        mc.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, mc.player.onGround)));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send out of bounds packet and confirm with predicted id
        mc.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(mc.player.posX, getBounds(), mc.player.posZ, yaw, pitch, mc.player.onGround)));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send new position and confirm again
        mc.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, mc.player.onGround)));
        if(extraPacket.getValue()) mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
        if(extraPacket.getValue()) mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId + 1));
    }

    private void setbackMove(double x, double y, double z, float yaw, float pitch) {
        // send out of bounds packet and confirm with predicted id
        mc.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(mc.player.posX, getBounds(), mc.player.posZ, yaw, pitch, mc.player.onGround)));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(++tpId));

        // send new position and confirm again
        mc.player.connection.sendPacket(add(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, mc.player.onGround)));
        if(extraPacket.getValue()) mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
        if(extraPacket.getValue()) mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId + 1));
    }

    private double getBounds() {
        return (bounds.getValue() == Bounds.DOWN ? mc.player.posY - 1850 : (bounds.getValue() == Bounds.UP ? mc.player.posY + 1850 : getGround()));
    }

    private double getGround() {
        BlockPos pos = mc.player.getPosition();
        while(mc.world.getBlockState((pos = pos.down())).getBlock() == Blocks.AIR) {  }
        return pos.getY();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketPlayerPosLook) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

                serverX = packet.getX();
                serverY = packet.getY();
                serverZ = packet.getZ();
                tpId = packet.getTeleportId();

                if(smooth.getValue()) {
                    ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, (SPacketPlayerPosLook) event.getPacket(), mc.player.rotationPitch, "pitch", "field_148937_e");
                    ReflectionHelper.setPrivateValue(SPacketPlayerPosLook.class, (SPacketPlayerPosLook) event.getPacket(), mc.player.rotationYaw, "yaw", "field_148936_d");
                }
            }

            if(noPacketKick.getValue() && event.getPacket() instanceof SPacketCloseWindow) event.setCanceled(true);
        } else if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayer) {
                if(packets.contains((CPacketPlayer) event.getPacket())) packets.remove((CPacketPlayer) event.getPacket());
                else event.setCanceled(true);
                if(!event.isCanceled() && smooth.getValue()) {
                    ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), pitch, "pitch", "field_149473_f");
                    ReflectionHelper.setPrivateValue(CPacketPlayer.class, (CPacketPlayer) event.getPacket(), yaw, "yaw", "field_149476_e");
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(BlockPushEvent event) {
        event.setCanceled(true);
    }

    private <T extends CPacketPlayer> T add(T packet) {
        packets.add(packet);
        return packet;
    }

}
