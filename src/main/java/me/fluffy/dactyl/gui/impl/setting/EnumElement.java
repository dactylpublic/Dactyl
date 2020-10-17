package me.fluffy.dactyl.gui.impl.setting;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class EnumElement extends SettingElement{
    public EnumElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    @Override
    public void drawScreen(int drawX, int drawY, float partialTicks) {
        this.drawDefaultBackground(drawX, drawY, true);
        Dactyl.fontUtil.drawString(getSetting().getName() + " " + TextFormatting.GRAY + getEnumName((Enum) getSetting().getValue()), this.getX()+4, this.getY() + (15 - 8) / 2, 0xffffffff);
    }

    @Override
    public void mouseClicked(int mouseButton, int x, int y) {
        if (isHovering(x, y)) {
            Enum[] vals = ((Enum)getSetting().getValue()).getClass().getEnumConstants();
            getSetting().setValue(vals[(((Enum)getSetting().getValue()).ordinal()+1) % vals.length]);
        }
    }

    private String getEnumName(Enum en) {
        return en.name().toLowerCase().substring(0, 1).toUpperCase() + en.name().toLowerCase().substring(1);
    }
}
