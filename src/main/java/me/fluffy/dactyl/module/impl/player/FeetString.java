package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FeetString extends Module {
    public Setting<Boolean> rotate = new Setting<Boolean>("Rotate", false);
    public FeetString() {
        super("FeetString", Category.PLAYER, "Places string at the players feet and is supposed to help make crystals cause less damage to self.");
    }

    @Override
    public void onEnable() {
        if(mc == null || mc.world == null || mc.player == null) {
            this.toggle();
            return;
        }
        int stringslot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock)) {
                if(stack.getItem().equals(Items.STRING)) {
                    stringslot = i;
                    break;
                }
            }
        }
        if(stringslot == -1) {
            this.toggle();
            return;
        }
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos placePosition = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        CombatUtil.placeBlockBurrow(placePosition, false, rotate.getValue(), false, true, true, stringslot);
        this.toggle();
    }
}
