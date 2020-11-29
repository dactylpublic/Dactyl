package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoRespawn extends Module {
    public Setting<Boolean> antiDeathScreen = new Setting<Boolean>("AntiDeathScreen", true);
    public AutoRespawn() {
        super("AutoRespawn", Category.MISC);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if (mc.player.getHealth() <= 0.0f || (this.antiDeathScreen.getValue() && AutoRespawn.mc.player.getHealth() > 0.0f)) {
                event.setCanceled(true);
                mc.player.respawnPlayer();
            }
        }
    }
}
