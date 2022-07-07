package me.chloe.dactyl.module.impl.client;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;

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
