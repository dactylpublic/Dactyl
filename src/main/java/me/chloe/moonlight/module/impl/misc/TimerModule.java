package me.chloe.moonlight.module.impl.misc;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.injection.inj.access.ITimer;
import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;

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
            ((ITimer)mc.timer).setTickLength(50f / (Moonlight.tickRateManager.getTickRate() / 20F));
            this.setModuleInfo(String.format("%.01f", (Moonlight.tickRateManager.getTickRate() / 20F)));
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
