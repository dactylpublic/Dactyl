package me.chloe.dactyl.module.impl.misc;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.module.Module;

import java.util.ArrayList;

public class Spammer extends Module {
    public Setting<Double> delay = new Setting<Double>("Delay", 1.0d, 0.1d, 10.0d);
    public static Spammer INSTANCE;
    public Spammer() {
        super("Spammer", Category.MISC);
        INSTANCE = this;
    }

    public ArrayList<String> spammerText = new ArrayList<>();
    private final TimeUtil timer = new TimeUtil();
    int offset = 0;

    @Override
    public void onToggle() {
        spammerText.clear();
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.player.connection == null) {
            return;
        }
        this.setModuleInfo("File");
        if(spammerText.size() == 0) {
            return;
        }
        if(timer.hasPassed(delay.getValue().longValue()*1000)) {
            if(offset >= spammerText.size()) {
                offset = 0;
            }
            if(spammerText.get(offset) != null) {
                mc.player.sendChatMessage(spammerText.get(offset));
                offset++;
                timer.reset();
            }
        }
    }

    public void reset() {
        timer.reset();
        offset = 0;
    }

}
