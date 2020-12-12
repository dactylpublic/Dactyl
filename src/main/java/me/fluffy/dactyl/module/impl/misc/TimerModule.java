package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class TimerModule extends Module {
    Setting<Boolean> tpsSync = new Setting<Boolean>("TPSSync", false);
    Setting<Double> speed = new Setting<Double>("Speed", 2.0d, 0.2d, 20.0d, v->!tpsSync.getValue());
    public TimerModule() {
        super("Timer", Category.MISC);
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null || mc.timer == null) {
            return;
        }
        if(tpsSync.getValue()) {
            ((ITimer)mc.timer).setTickLength(50f / (Dactyl.tickRateManager.getTickRate() / 20F));
            this.setModuleInfo(String.format("%.01f", (Dactyl.tickRateManager.getTickRate() / 20F)));
        } else {
            ((ITimer)mc.timer).setTickLength(50f / speed.getValue().floatValue());
            this.setModuleInfo(String.format("%.01f", speed.getValue()));
        }
    }

    @Override
    public void onToggle() {
        if(mc.timer == null) {
            return;
        }
        ((ITimer)mc.timer).setTickLength(50f);
    }
}
