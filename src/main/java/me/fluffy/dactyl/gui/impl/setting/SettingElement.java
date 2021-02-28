package me.fluffy.dactyl.gui.impl.setting;

import me.fluffy.dactyl.gui.ClickGUI;
import me.fluffy.dactyl.module.impl.client.ClickGuiModule;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.render.RenderUtil;

public class SettingElement {
    private int x, y;
    private Setting setting;
    private boolean visible;
    public SettingElement(Setting setting, int x, int y) {
        this.setting = setting;
        this.x = x;
        this.y = y;
    }

    public void drawScreen(int drawX, int drawY, float partialTicks) {

    }

    public void mouseClicked(int mouseButton, int mouseX, int mouseY) {

    }

    public void onKeyPressed(char eventChar, int key) {

    }

    public void drawDefaultBackground(int mouseX, int mouseY, boolean color) {
        int startColor = ClickGUI.INSTANCE.getColorHovering(this.y, this.x, this.y, mouseX, mouseY, !color, false);
        int endColor = ClickGUI.INSTANCE.getColorHovering(this.y+15, this.x, this.y, mouseX, mouseY, !color, false);
        //int c = ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, !color, false);
        if(ClickGuiModule.INSTANCE.gradientGui.getValue()) {
            RenderUtil.drawGradientRect(x, y, x + 100, y + 15, startColor, endColor);
        } else {
            startColor = ClickGUI.INSTANCE.getColorHoveringNonGradient(1, this.x, this.y, mouseX, mouseY, !color, false);
            RenderUtil.drawRect(x, y, x + 100, y + 15, startColor);
        }
        //RenderUtil.drawRect(x, y, x+100, y+15, c);
        RenderUtil.drawOutlinedRectangle(x, y, x+100, y+15, 0xff2e2e2e);
    }

    public void drawBlackBackground() {
        RenderUtil.drawGradientRect(x, y, x+100, y+15, 0x57000000, 0x57000000);
        RenderUtil.drawOutlinedRectangle(x, y, x+100, y+15, 0xff2e2e2e);
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x+100 && mouseY >= y && mouseY <= y + 15;
    }

    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Setting getSetting() {
        return this.setting;
    }
}
