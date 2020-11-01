package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
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
