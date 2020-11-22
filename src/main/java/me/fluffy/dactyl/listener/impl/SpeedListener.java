package me.fluffy.dactyl.listener.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.listener.Listener;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.DecimalFormat;
import java.util.HashMap;

public class SpeedListener extends Listener {
    public static SpeedListener INSTANCE;

    public SpeedListener() {
        super("SpeedListener", "Handles speed calculations.");
        INSTANCE = this;
    }
    private static Minecraft mc = Minecraft.getMinecraft();


    double blocksPerTick = 0.0d;
    double[] bptMap = new double[30];
    int offset = 0;
    int updateOffset = 0;

    @SubscribeEvent
    public void onClientUpdate(TickEvent.ClientTickEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        updateOffset++;
        if(updateOffset >= 2) {
            updateValues();
            updateOffset = 0;
        }
    }

    public void updateValues() {
        blocksPerTick = Math.sqrt(((mc.player.posX - mc.player.prevPosX)*(mc.player.posX - mc.player.prevPosX))+((mc.player.posZ - mc.player.prevPosZ)*(mc.player.posZ - mc.player.prevPosZ)));
        //blocksPerTick = Math.sqrt(((mc.player.posX - mc.player.lastTickPosX)*(mc.player.posX - mc.player.lastTickPosX))+((mc.player.posZ - mc.player.lastTickPosZ)*(mc.player.posZ - mc.player.lastTickPosZ)));
        if(offset > 29) {
            offset = 0;
        }
        bptMap[offset] = blocksPerTick;
        offset++;
    }

    public double getSpeedKpH() {
        double avgTotal = 0.0d;
        for(int i = 0; i < 30; i++) {
            avgTotal+=bptMap[i];
        }
        double currentBPTAverage = avgTotal/30;
        if(mc.player.moveForward == 0.0f && mc.player.moveStrafing == 0.0f) {
            return 0.0d;
        } else {
            return ((currentBPTAverage * 20) * 3.6);
        }
    }
}
