package me.chloe.moonlight.gui.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.gui.impl.setting.SettingElement;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.module.impl.client.ClickGuiModule;
import me.chloe.moonlight.module.impl.client.Colors;
import me.chloe.moonlight.util.render.RenderUtil;

import java.util.ArrayList;

public class ModulePanel {
    private final Module.Category category;
    private final int x;
    private int y;
    public final ArrayList<ModuleButton> moduleButtons = new ArrayList<>();
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
        if(ClickGuiModule.INSTANCE.gradientGui.getValue()) {
            RenderUtil.drawRect(x, y, x + 100, y + 15, Colors.INSTANCE.getColor(y + 15, false));
        } else {
            RenderUtil.drawRect(x, y, x + 100, y + 15, Colors.INSTANCE.getColor(1, false));
        }
        //RenderUtil.drawRect(x, y, x+100, y+15, 0xff004fcf);
        Moonlight.fontUtil.drawString(category.toString(), x+4, y + (15 - 8) / 2, 0xffffffff);
        RenderUtil.drawOutlinedRectangle(x, y, x+100, y+15, 0xff2e2e2e);
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
        for(Module module : Moonlight.moduleManager.getModules()) {
            if(module.getCategory() == this.category) {
                moduleButtons.add(new ModuleButton(this, module, this.x, this.y+moduleY));
                moduleY+=15;
            }
        }
    }

    public void setY(int y) {
        int oldy = this.y;
        this.y = y;
        for(ModuleButton e : moduleButtons) {
            e.setY(e.getY()+(y-oldy));
            for(SettingElement settingElement : e.getSettingElements()) {
                if(settingElement.getSetting().isVisible()) {
                    settingElement.setY(settingElement.getY()+(y-oldy));
                }
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
