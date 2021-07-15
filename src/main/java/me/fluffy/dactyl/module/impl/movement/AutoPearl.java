package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class AutoPearl extends Module {
    public AutoPearl() {
        super("AutoPearl", Category.MOVEMENT, "Middle click pearl");
    }
    private boolean clicked = false;

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if (Mouse.isButtonDown(2)) {
            if (!this.clicked) {
                this.throwPearl();
            }
            this.clicked = true;
        } else {
            this.clicked = false;
        }
    }

    private void throwPearl() {
        if(mc.player != null && mc.world != null && mc.currentScreen != null) {
            return;
        }
        final RayTraceResult result = mc.objectMouseOver;
        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
            Entity entity = result.entityHit;
            if (entity instanceof EntityPlayer) {
                return;
            }
        }
        final int pearlSlot = CombatUtil.findHotbarBlock(ItemEnderPearl.class);
        final boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        if (pearlSlot != -1 || offhand) {
            final int oldslot = mc.player.inventory.currentItem;
            if (!offhand) {
                mc.player.inventory.currentItem = pearlSlot;
            }
            mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (!offhand) {
                mc.player.inventory.currentItem = oldslot;
            }
        }
    }
}
