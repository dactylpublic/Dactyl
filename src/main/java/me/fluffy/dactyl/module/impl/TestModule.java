package me.fluffy.dactyl.module.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import org.lwjgl.input.Keyboard;

public class TestModule extends Module {
    public TestModule() {
        super("TestModule", Category.MISC, "for testing new client");
        this.setKey(Keyboard.KEY_V);
    }

    @Override
    public void onToggle() {
        Dactyl.logger.info("it worked :D");
    }
}
