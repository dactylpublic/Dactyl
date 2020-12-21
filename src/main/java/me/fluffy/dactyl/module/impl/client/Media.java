package me.fluffy.dactyl.module.impl.client;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class Media extends Module {
    Setting<String> fakeUsername = new Setting<String>("Username", "DactylUser");


    public static Media INSTANCE;
    public Media() {
        super("Media", Category.CLIENT);
        INSTANCE = this;
    }

    public String onRenderString(String s) {
        if(!this.isEnabled()) {
            return s;
        }
        if(mc.getSession().getUsername() == null) {
            return s;
        }
        s = s.replace(mc.getSession().getUsername(), fakeUsername.getValue());
        return s.replace(mc.getSession().getUsername(), fakeUsername.getValue());
    }
}
