package me.chloe.moonlight.gui.impl.setting;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.setting.Setting;

public class BooleanElement extends SettingElement {
    public BooleanElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    @Override
    public void drawScreen(int drawX, int drawY, float partialTicks) {
        this.drawDefaultBackground(drawX, drawY, (Boolean) this.getSetting().getValue());
        Moonlight.fontUtil.drawString(this.getSetting().getName(), this.getX() + 4, this.getY() + (15 - 8) / 2, 0xffffffff);
    }

    @Override
    public void mouseClicked(int mouseButton, int x, int y) {
        if(mouseButton != 0) {
            return;
        }
        if (isHovering(x, y)) {
            boolean currentVal = (boolean) this.getSetting().getValue();
            this.getSetting().setValue(!currentVal);
        }
    }
}
