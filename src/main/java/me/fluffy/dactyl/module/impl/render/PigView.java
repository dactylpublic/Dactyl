package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.module.Module;

// gonna be the fucking best module ever.
// DONT COME TO CONSTANTIAM SPAWN ON 15/05/2021. WORST MISTAKE OF MY LIFE
// the one module i actually wrote myself

// nevermind it looks like im eating the pigs ass. idk how to make pig opacity change so i guess this is useless :(
// https://imgur.com/a/izZbMwg < Look at dis

public class PigView extends Module {

    public static PigView INSTANCE;

    public PigView() {
        super("PigView", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onClientUpdate() {
        mc.player.eyeHeight = 0.6f;
    }

    public void onDisable() {
        mc.player.eyeHeight = mc.player.getDefaultEyeHeight();
    }
}