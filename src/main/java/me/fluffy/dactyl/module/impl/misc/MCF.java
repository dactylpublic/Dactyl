package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
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
