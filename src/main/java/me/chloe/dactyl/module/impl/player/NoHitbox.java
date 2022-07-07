package me.chloe.dactyl.module.impl.player;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;
import net.minecraft.item.ItemPickaxe;

public class NoHitbox extends Module {
    Setting<Boolean> onlyPickaxe = new Setting<Boolean>("OnlyPickaxe", true);

    public static NoHitbox INSTANCE;
    public NoHitbox() {
        super("NoHitbox", Category.PLAYER);
        INSTANCE = this;
    }

    public boolean doRemoveEntities() {
        return (mc.objectMouseOver != null && this.isEnabled() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && onlyPickaxe.getValue()));
    }
}
