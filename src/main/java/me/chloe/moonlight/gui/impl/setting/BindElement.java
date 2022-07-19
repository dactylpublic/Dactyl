package me.chloe.moonlight.gui.impl.setting;

import me.chloe.moonlight.util.Bind;
import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.setting.Setting;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class BindElement extends SettingElement {
    public boolean listening;
    public BindElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    @Override
    public void drawScreen(int drawX, int drawY, float partialTicks) {
        this.drawDefaultBackground(drawX, drawY, listening);
        String currentText = listening ? "Listening..." : Keyboard.getKeyName(((Bind)getSetting().getValue()).getKey());
        Moonlight.fontUtil.drawString(getSetting().getName() + " " + TextFormatting.GRAY + currentText, this.getX()+4, this.getY() + (15 - 8) / 2, 0xffffffff);
    }

    @Override
    public void mouseClicked(int mouseButton, int x, int y) {
        if (isHovering(x, y) && !listening) {
            if(mouseButton == 0) {
                listening = true;
            }
        } else if(isHovering(x, y) && listening){
            if(mouseButton != 0 && mouseButton != 1) {
                getSetting().setValue(new Bind(mouseButton));
                listening = false;
            }
        }
    }

    @Override
    public void onKeyPressed(char eventChar, int key) {
        if (listening) {
            if (key == Keyboard.KEY_DELETE) {
                getSetting().setValue(new Bind(Keyboard.KEY_NONE));
            } else {
                getSetting().setValue(new Bind(key));
            }
            listening = false;
        }
    }
}
