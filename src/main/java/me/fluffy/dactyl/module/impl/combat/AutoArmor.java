package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class AutoArmor extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 100, 1, 1000);
    public Setting<Boolean> mendWhileXP = new Setting<Boolean>("XPMend", false);
    public AutoArmor() {
        super("AutoArmor", Category.COMBAT);
    }
}
