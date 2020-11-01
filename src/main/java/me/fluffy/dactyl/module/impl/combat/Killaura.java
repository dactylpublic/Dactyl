package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class Killaura extends Module {
    Setting<AttackMode> attackLogic = new Setting<AttackMode>("Logic", AttackMode.HELD);
    Setting<Boolean> hitDelay = new Setting<Boolean>("HitDelay", true);
    Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    Setting<Boolean> players = new Setting<Boolean>("Players", true);
    Setting<Boolean> mobs = new Setting<Boolean>("Mobs", false);
    Setting<Boolean> animals = new Setting<Boolean>("Animals", false);
    Setting<Boolean> vehicles = new Setting<Boolean>("Vehicles", true);
    Setting<Boolean> autoSwitch = new Setting<Boolean>("AutoSwitch", false);
    Setting<Integer> attackSpeed = new Setting<Integer>("AttackSpeed", 12, 1, 20, v->!hitDelay.getValue());
    Setting<Double> range = new Setting<Double>("Range", 4.5D, 1.0D, 6.0D);
    public Killaura() {
        super("Killaura", Category.COMBAT);
    }



    public enum AttackMode {
        HELD,
        ALWAYS
    }
}
