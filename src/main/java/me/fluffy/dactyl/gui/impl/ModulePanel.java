package me.fluffy.dactyl.gui.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class ModulePanel {
    private final Module.Category category;
    private final int x;
    private final int y;
    private final ArrayList<ModuleButton> moduleButtons = new ArrayList<>();
    public ModulePanel(Module.Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        setupModules();
    }

    public void drawScreen(int drawX, int drawY, float partialTicks) {
        drawCategoryButton();
        drawModuleButtons(drawX, drawY, partialTicks);
    }

    private void drawModuleButtons(int x, int y, float partialTicks) {
        for(ModuleButton button : moduleButtons) {
            button.drawScreen(x, y, partialTicks);
        }
    }

    public void mouseClicked(int mouseButton, int mouseX, int mouseY) {
        for(ModuleButton moduleButton : moduleButtons) {
            moduleButton.mouseClicked(mouseButton, mouseX, mouseY);
        }
    }

    private void drawCategoryButton() {
        RenderUtil.drawRect(x, y, x+100, y+15, 0xff004fcf);
        Dactyl.fontUtil.drawString(category.toString(), x+4, y + (15 - 8) / 2, 0xffffffff);
        RenderUtil.drawOutlinedRectange(x, y, x+100, y+15, 0xff2e2e2e);
    }

    public void onKeyPressed(char eventChar, int key) {
        for(ModuleButton moduleButton : moduleButtons) {
            moduleButton.onKeyPressed(eventChar, key);
        }
    }

    public ArrayList<ModuleButton> getModuleButtons() {
        return this.moduleButtons;
    }

    public void setupModules() {
        int moduleY = 16;
        for(Module module : Dactyl.moduleManager.getModules()) {
            if(module.getCategory() == this.category) {
                moduleButtons.add(new ModuleButton(this, module, this.x, this.y+moduleY));
                moduleY+=15;
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public Module.Category getCategory() {
        return this.category;
    }
}
