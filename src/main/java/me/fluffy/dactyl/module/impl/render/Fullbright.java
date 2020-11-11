package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.stats.StatBase;

public class Fullbright extends Module {
    public Setting<Mode> modeSetting = new Setting<Mode>("Mode", Mode.POTION);
    public Fullbright() {
        super("FullBright", Category.RENDER);
    }

    float oldGamma = -1f;

    private TimeUtil timer = new TimeUtil();

    @Override
    public void onEnable() {

        oldGamma = mc.gameSettings.gammaSetting;
        if(modeSetting.getValue() == Mode.GAMMA) {
            mc.gameSettings.gammaSetting = 1000f;
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null) {
            return;
        }
        if(timer.hasPassed(500)) {
            if(modeSetting.getValue() == Mode.POTION && !mc.player.isPotionActive(Potion.getPotionById(16))) {
                PotionEffect nightVision = new PotionEffect(Potion.getPotionById(16), Integer.MAX_VALUE, 0);
                nightVision.setPotionDurationMax(true);
                mc.player.addPotionEffect(nightVision);
            }
        }
    }

    @Override
    public void onDisable() {
        if(mc.player == null) {
            return;
        }
        mc.gameSettings.gammaSetting = oldGamma;
        mc.player.removeActivePotionEffect(Potion.getPotionById(16));
        timer.reset();
    }

    private enum Mode {
        GAMMA,
        POTION
    }
}
