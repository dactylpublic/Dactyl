package me.fluffy.dactyl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class EntityUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int getPing(EntityPlayer p) {
        int ping = 0;
        try {
            ping = (int) mc.getConnection().getPlayerInfo(p.getUniqueID()).getResponseTime();
        } catch (NullPointerException np) {}
        return ping;
    }
}
