package me.chloe.dactyl.module.impl.misc;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

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
            if(!Dactyl.friendManager.isFriend(player.getName())) {
                Dactyl.friendManager.addFriend(player.getName());
                ChatUtil.printMsg("&7Friend " + player.getName() + " added.", true, true);
            } else {
                Dactyl.friendManager.removeFriend(player.getName());
                ChatUtil.printMsg("&7Friend " + player.getName() + " removed.", true, true);
            }
        }
    }
}
