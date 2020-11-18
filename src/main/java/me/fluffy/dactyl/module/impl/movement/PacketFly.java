package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PacketFly extends Module {
    public Setting<FlyMode> modeSetting = new Setting<FlyMode>("Mode", FlyMode.FAST);
    public Setting<Double> yDist = new Setting<Double>("YDist", 999.0d, 0.0d, 999.0d);
    public Setting<PacketMode> packetModeSetting = new Setting<PacketMode>("Y", PacketMode.DOWN);
    public Setting<Boolean> wait = new Setting<Boolean>("Wait", true, v->modeSetting.getValue() == FlyMode.NORMAL);
    public Setting<Double> waitTime = new Setting<Double>("WaitTime", 2.5d, 0.0d, 5.0d, v->modeSetting.getValue() == FlyMode.NORMAL);
    public Setting<Double> waitOffset = new Setting<Double>("Wait", 5.0d, 0.0d, 10.0d, v->modeSetting.getValue() == FlyMode.NORMAL);
    public Setting<Double> speed = new Setting<Double>("Speed", 1.0d, 0.05d, 1.0d);



    public PacketFly() {
        super("PacketFly", Category.MOVEMENT);
    }

    private final TimeUtil timer = new TimeUtil();
    public int teleportID;
    public List<CPacketPlayer> playerPacketList = new ArrayList<>();


    @SubscribeEvent
    public void onPlayerUpdate(EventUpdateWalkingPlayer event) {
        this.setModuleInfo(modeSetting.getValue() == FlyMode.FAST ? "Fast" : "Normal");
        if(mc.player.isDead) {
            return;
        }

        if(modeSetting.getValue() == FlyMode.FAST) {
            if (this.teleportID <= 0) {
                final double posX = mc.player.posX;
                final double posY = mc.player.posY;
                double doubleValue = (packetModeSetting.getValue() == PacketMode.DOWN ? (-yDist.getValue()) : yDist.getValue());
                final CPacketPlayer.Position position = new CPacketPlayer.Position(posX, posY + doubleValue, mc.player.posZ, mc.player.onGround);
                this.playerPacketList.add((CPacketPlayer) position);
                mc.player.connection.sendPacket(position);
                return;
            }
            mc.player.setVelocity(0.0, 0.0, 0.0);
            double n2;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                double n;
                if (mc.player.ticksExisted % 20 == 0) {
                    n = -0.03999999910593033;
                } else {
                    n = 0.06199999898672104;
                }
                n2 = n;
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                n2 = -0.062;
            } else {
                double n3;
                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty()) {
                    if (mc.player.ticksExisted % 20 == 0) {
                        n3 = -0.03999999910593033;
                    } else {
                        n3 = 0.0;
                    }
                } else {
                    n3 = 0.0;
                }
                n2 = n3;
            }
            final double[] array = calcSpeed(this.speed.getValue());
            if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().grow(0.0625, 0.0, 0.0625)).isEmpty()) {
                if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.setVelocity(0.0, 0.0, 0.0);
                    this.doBoundingBox(0.0, 0.0, 0.0);
                    int i = 0;
                    while (i <= 3) {
                        mc.player.setVelocity(0.0, n2 * i, 0.0);
                        this.doBoundingBox(0.0, n2 * i, 0.0);
                        ++i;
                    }
                    return;
                }
            }
            if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown()) {
                if (array[0] != 0.0 || n2 != 0.0 || array[1] != 0.0) {
                    if ((mc.player.movementInput.jump && (mc.player.moveStrafing != 0.0f || mc.player.moveForward != 0.0f)) || mc.player.movementInput.jump) {
                        mc.player.setVelocity(0.0, 0.0, 0.0);
                        this.doBoundingBox(0.0, 0.0, 0.0);
                        int j = 0;
                        while (j <= 3) {
                            mc.player.setVelocity(0.0, n2 * j, 0.0);
                            this.doBoundingBox(0.0, n2 * j, 0.0);
                            ++j;
                        }
                    } else {
                        int k = 0;
                        while (k <= 2) {
                            mc.player.setVelocity(array[0] * k, n2 * k, array[1] * k);
                            this.doBoundingBox(array[0] * k, n2 * k, array[1] * k);
                            ++k;
                        }
                    }
                }
            } else if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty()) {
                final double n4 = 0.0;
                double n5;
                if (mc.player.ticksExisted % 2 == 0) {
                    n5 = 0.03999999910593033;
                } else {
                    n5 = -0.03999999910593033;
                }
                mc.player.setVelocity(n4, n5, 0.0);
                final double n6 = 0.0;
                double n7;
                if (mc.player.ticksExisted % 2 == 0) {
                    n7 = 0.03999999910593033;
                } else {
                    n7 = -0.03999999910593033;
                }
                this.doBoundingBox(n6, n7, 0.0);
            }
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(modeSetting.getValue() == FlyMode.NORMAL) {
            event.setMotion(0, 0, 0);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(modeSetting.getValue() == FlyMode.FAST) {
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.setCanceled(true);
                }
                if (event.getPacket() instanceof CPacketPlayer) {
                    final CPacketPlayer cPacketPlayer = (CPacketPlayer) event.getPacket();
                    if (this.playerPacketList.contains(cPacketPlayer)) {
                        this.playerPacketList.remove(cPacketPlayer);
                        return;
                    }
                    event.setCanceled(true);
                }
            }
        } else if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(modeSetting.getValue() == FlyMode.FAST) {
                if(event.getPacket() instanceof SPacketPlayerPosLook) {
                    final SPacketPlayerPosLook sPacketPlayerPosLook = (SPacketPlayerPosLook) event.getPacket();
                    if (mc.player != null && mc.player.isEntityAlive() && mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
                        if (this.teleportID <= 0) {
                            this.teleportID = sPacketPlayerPosLook.getTeleportId();
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        this.teleportID = 0;
        this.playerPacketList.clear();
        if (modeSetting.getValue() == FlyMode.FAST) {
            final double posX = mc.player.posX;
            final double posY = mc.player.posY;
            double doubleValue = (packetModeSetting.getValue() == PacketMode.DOWN ? (-yDist.getValue()) : yDist.getValue());
            final CPacketPlayer.Position position = new CPacketPlayer.Position(posX, posY + doubleValue, mc.player.posZ, mc.player.onGround);
            this.playerPacketList.add((CPacketPlayer) position);
            mc.player.connection.sendPacket(position);
        }
    }


    public void doBoundingBox(final double n, final double n2, final double n3) {
        final CPacketPlayer.Position position = new CPacketPlayer.Position(mc.player.posX + n, mc.player.posY + n2, mc.player.posZ + n3, mc.player.onGround);
        this.playerPacketList.add((CPacketPlayer)position);
        mc.player.connection.sendPacket(position);
        final double n4 = mc.player.posX + n;
        final double posY = mc.player.posY;
        double doubleValue = (packetModeSetting.getValue() == PacketMode.DOWN ? (-yDist.getValue()) : yDist.getValue());
        final CPacketPlayer.Position position2 = new CPacketPlayer.Position(n4, posY + doubleValue, mc.player.posZ + n3, mc.player.onGround);
        this.playerPacketList.add((CPacketPlayer)position2);
        mc.player.connection.sendPacket(position2);
        ++this.teleportID;
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportID - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportID));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportID + 1));
    }

    public static double[] calcSpeed(final double speed) {
        float n2 = 0.0f;
        float n3 = 0.0f;
        final double n4 = 2.7999100260353087 * speed;
        final float sin = MathHelper.sin(mc.player.rotationYaw * 3.1415927f / 180.0f);
        final float cos = MathHelper.cos(mc.player.rotationYaw * 3.1415927f / 180.0f);
        if (mc.player.movementInput.forwardKeyDown && !mc.player.movementInput.backKeyDown && !mc.player.movementInput.leftKeyDown && !mc.player.movementInput.rightKeyDown) {
            n2 += 0.1f;
        }
        else if (!mc.player.movementInput.forwardKeyDown && mc.player.movementInput.backKeyDown && !mc.player.movementInput.leftKeyDown && !mc.player.movementInput.rightKeyDown) {
            n2 -= 0.1f;
        }
        else if (!mc.player.movementInput.forwardKeyDown && !mc.player.movementInput.backKeyDown && mc.player.movementInput.leftKeyDown && !mc.player.movementInput.rightKeyDown) {
            n3 += 0.1f;
        }
        if (!mc.player.movementInput.forwardKeyDown && !mc.player.movementInput.backKeyDown && !mc.player.movementInput.leftKeyDown && mc.player.movementInput.rightKeyDown) {
            n3 -= 0.1f;
        }
        else if (mc.player.movementInput.forwardKeyDown && !mc.player.movementInput.backKeyDown && mc.player.movementInput.leftKeyDown && !mc.player.movementInput.rightKeyDown) {
            n2 += 0.0624f;
            n3 += 0.0624f;
        }
        else if (mc.player.movementInput.forwardKeyDown && !mc.player.movementInput.backKeyDown && !mc.player.movementInput.leftKeyDown && mc.player.movementInput.rightKeyDown) {
            n2 += 0.0624f;
            n3 -= 0.0624f;
        }
        else if (!mc.player.movementInput.forwardKeyDown && mc.player.movementInput.backKeyDown && mc.player.movementInput.leftKeyDown && !mc.player.movementInput.rightKeyDown) {
            n2 -= 0.0624f;
            n3 += 0.0624f;
        }
        else if (!mc.player.movementInput.forwardKeyDown && mc.player.movementInput.backKeyDown && !mc.player.movementInput.leftKeyDown && mc.player.movementInput.rightKeyDown) {
            n2 -= 0.0624f;
            n3 -= 0.0624f;
        }
        return new double[] { (n3 * cos - n2 * sin) * n4, (n2 * cos + n3 * sin) * n4 };
    }

    public enum FlyMode {
        FAST,
        NORMAL
    }

    private enum PacketMode {
        UP,
        DOWN
    }
}
