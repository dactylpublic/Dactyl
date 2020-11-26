package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class AntiVoid extends Module {
    public Setting<AntiMode> antiModeSetting = new Setting<AntiMode>("Mode", AntiMode.HOVER);
    public AntiVoid() {
        super("AntiVoid", Category.MOVEMENT);
    }


    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(mc.player.posY <= 0.5d) {
            switch((AntiMode)antiModeSetting.getValue()) {
                case HOVER:
                    mc.player.motionY += 0.07D;
                    break;
                case VCLIP:
                    mc.player.moveVertical = 5.0f;
                    mc.player.jump();
                    break;
                case SPRING:
                    mc.player.moveVertical = 10.0f;
                    mc.player.jump();
                    break;
            }
        }
        this.setModuleInfo(antiModeSetting.getValue().toString());
    }


    private enum AntiMode {
        HOVER("Hover"),
        VCLIP("VClip"),
        SPRING("Spring");

        private final String name;

        private AntiMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
