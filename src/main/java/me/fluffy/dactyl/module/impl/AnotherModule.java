package me.fluffy.dactyl.module.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import org.lwjgl.input.Keyboard;

public class AnotherModule extends Module {
    Setting<TestEnum> visTest = new Setting<TestEnum>("Visibility", TestEnum.TEST1);
    Setting<Integer> testSetting = new Setting<Integer>("TestSetting", 1, 0, 10, vis->visTest.getValue() == TestEnum.TEST1);
    Setting<Double> testSetting2 = new Setting<Double>("Double", 1.0D, 0.0D, 6.6D, vis->visTest.getValue() == TestEnum.TEST1);
    //Setting<Float> testSetting3 = new Setting<Float>("Float", 1.0F, 0.0F, 6.9F, vis->visTest.getValue() == TestEnum.VISIBLE);
    public AnotherModule() {
        super("AnotherModule", Category.MISC, "for testing new client part 6");
        this.setKey(Keyboard.KEY_X);
    }

    @Override
    public void onToggle() {
        Dactyl.logger.info("it worked :D ezpz get pwned noob");
    }


    private enum TestEnum {
        TEST1,
        TEST2
    }
}
