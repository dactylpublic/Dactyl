package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

/*
* @author 3arthqu4ke
* Taken from phobos as speed calculations are annoying as hell.
 */

public class SpeedListener extends Listener {
    public static SpeedListener INSTANCE;

    public SpeedListener() {
        super("SpeedListener", "Handles speed calculations.");
        INSTANCE = this;
    }

    public double firstJumpSpeed = 0.0D;

    public double lastJumpSpeed = 0.0D;

    public double percentJumpSpeedChanged = 0.0D;

    public double jumpSpeedChanged = 0.0D;

    public static boolean didJumpThisTick = false;

    public static boolean isJumping = false;

    public boolean didJumpLastTick = false;

    public long jumpInfoStartTime = 0L;

    public boolean wasFirstJump = true;

    public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0D;

    public double speedometerCurrentSpeed = 0.0D;

    public HashMap<EntityPlayer, Double> playerSpeeds = new HashMap<>();

    private int distancer = 20;

    private static Minecraft mc = Minecraft.getMinecraft();

    public static void setDidJumpThisTick(boolean val) {
        didJumpThisTick = val;
    }

    public static void setIsJumping(boolean val) {
        isJumping = val;
    }

    public float lastJumpInfoTimeRemaining() {
        return (float)(Minecraft.getSystemTime() - this.jumpInfoStartTime) / 1000.0F;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(EventUpdateWalkingPlayer event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(event.getStage() == ForgeEvent.Stage.PRE) {
            updateValues();
        }
    }

    public void updateValues() {
        double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
        double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
        this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        if (didJumpThisTick && (!mc.player.onGround || isJumping)) {
            if (didJumpThisTick && !this.didJumpLastTick) {
                this.wasFirstJump = (this.lastJumpSpeed == 0.0D);
                this.percentJumpSpeedChanged = (this.speedometerCurrentSpeed != 0.0D) ? (this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0D) : -1.0D;
                this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
                this.jumpInfoStartTime = Minecraft.getSystemTime();
                this.lastJumpSpeed = this.speedometerCurrentSpeed;
                this.firstJumpSpeed = this.wasFirstJump ? this.lastJumpSpeed : 0.0D;
            }
            this.didJumpLastTick = didJumpThisTick;
        } else {
            this.didJumpLastTick = false;
            this.lastJumpSpeed = 0.0D;
        }
    }

    public void updatePlayers() {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (mc.player.getDistanceSq((Entity)player) < (this.distancer * this.distancer)) {
                double distTraveledLastTickX = player.posX - player.prevPosX;
                double distTraveledLastTickZ = player.posZ - player.prevPosZ;
                double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
                this.playerSpeeds.put(player, Double.valueOf(playerSpeed));
            }
        }
    }

    public double getPlayerSpeed(EntityPlayer player) {
        if (this.playerSpeeds.get(player) == null)
            return 0.0D;
        return turnIntoKpH((Double) this.playerSpeeds.get(player));
    }

    public double turnIntoKpH(double input) {
        return MathHelper.sqrt(input) * 71.2729367892D;
    }

    public double getSpeedKpH() {
        double speedometerkphdouble = turnIntoKpH(this.speedometerCurrentSpeed);
        speedometerkphdouble = Math.round(10.0D * speedometerkphdouble) / 10.0D;
        return speedometerkphdouble;
    }

    public double getSpeedMpS() {
        double speedometerMpsdouble = turnIntoKpH(this.speedometerCurrentSpeed) / 3.6D;
        speedometerMpsdouble = Math.round(10.0D * speedometerMpsdouble) / 10.0D;
        return speedometerMpsdouble;
    }
}
