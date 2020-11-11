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

    public static String getFacingWithProperCapitals() {
        String directionLabel = mc.getRenderViewEntity().getHorizontalFacing().getName().toLowerCase();
        switch (directionLabel) {
            case "north":
                directionLabel = "North";
                break;

            case "south":
                directionLabel = "South";
                break;

            case "west":
                directionLabel = "West";
                break;

            case "east":
                directionLabel = "East";
                break;
        }
        return directionLabel;
    }

    public static String getRelativeDirection() {
        String directionLabel = "";
        switch (getFacingWithProperCapitals().toLowerCase()) {
            case "north":
                directionLabel = "-Z";
                break;
            case "south":
                directionLabel = "+Z";
                break;
            case "west":
                directionLabel = "-X";
                break;

            case "east":
                directionLabel = "+X";
                break;
        }
        return directionLabel;
    }
}
