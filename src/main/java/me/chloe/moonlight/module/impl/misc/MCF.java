package me.chloe.moonlight.module.impl.misc;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

public class MCF extends Module {
    public static MCF INSTANCE;
    public MCF() {
        super("MCF", Category.MISC);
        INSTANCE = this;
    }

    public void doMiddleClick() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)mc.objectMouseOver.entityHit;
            if(!Moonlight.friendManager.isFriend(player.getName())) {
                Moonlight.friendManager.addFriend(player.getName());
                ChatUtil.printMsg("&7Friend " + player.getName() + " added.", true, true);
            } else {
                Moonlight.friendManager.removeFriend(player.getName());
                ChatUtil.printMsg("&7Friend " + player.getName() + " removed.", true, true);
            }
        }
    }
}
