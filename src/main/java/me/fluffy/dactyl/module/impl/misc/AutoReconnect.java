package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.injection.inj.access.IGuiDisconnected;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class AutoReconnect extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 5, 0, 10);

    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }
    private ServerData serverData = null;

    @Override
    public void onClientUpdate() {
        serverData = mc.getCurrentServerData() != null ? mc.getCurrentServerData() : serverData;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiDisconnected) {
            event.setGui(new GuiAutoReconnect((GuiDisconnected) event.getGui()));
        }
    }


    public class GuiAutoReconnect extends GuiDisconnected {
        private final long finalTime;

        public GuiAutoReconnect(GuiDisconnected guiDisconnected) {
            super(new GuiMultiplayer(new GuiMainMenu()), ((IGuiDisconnected)guiDisconnected).getReason(), ((IGuiDisconnected)guiDisconnected).getMessage());
            finalTime = (long) (System.currentTimeMillis() + (delay.getValue() * 1000));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);

            long time = finalTime - System.currentTimeMillis();
            if(time <= 0) mc.displayGuiScreen(new GuiConnecting(this, mc, serverData));

            String text = "Reconnecting in " + (time/1000) + "s";
            fontRenderer.drawStringWithShadow(text, width / 2 - fontRenderer.getStringWidth(text) / 2, 2, Color.WHITE.getRGB());
        }
    }
}
