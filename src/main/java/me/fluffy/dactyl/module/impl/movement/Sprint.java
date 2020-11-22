package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class Sprint extends Module {
    Setting<SprintMode> sprintModeSetting = new Setting<SprintMode>("Mode", SprintMode.RAGE);

    public Sprint() {
        super("Sprint", Category.MOVEMENT);
    }

    @Override
    public void onClientUpdate() {
        if(sprintModeSetting.getValue() == null) {
            return;
        }
        this.setModuleInfo(sprintModeSetting.getValue() == SprintMode.LEGIT ? "Legit" : "Rage");
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(canSprint()) {
            mc.player.setSprinting(true);
        }
    }


    private boolean canSprint() {
        switch((SprintMode) sprintModeSetting.getValue()) {
            case LEGIT:
                return (mc.player.moveForward > 0 && !mc.player.isActiveItemStackBlocking() && !mc.player.isOnLadder() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6);
            case RAGE:
                return ((mc.player.movementInput.moveStrafe != 0.0F || mc.player.moveForward != 0.0F) && !mc.player.isActiveItemStackBlocking() && !mc.player.isOnLadder() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6);
            default:
                return false;
        }
    }



    private enum SprintMode {
        LEGIT,
        RAGE
    }
}
