package me.fluffy.dactyl.gui.impl.setting;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class EnumElement extends SettingElement{
    public EnumElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    private static final String[] whitelistedWords = new String[] {"AAC", "NCP"};

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
        //if(StringUtil.isStringUpperCase(en.name()) && !Arrays.asList(whitelistedWords).contains(en.name())) {
        if(StringUtil.isStringUpperCase(en.toString()) && !Arrays.asList(whitelistedWords).contains(en.toString())) {
            return en.name().toLowerCase().substring(0, 1).toUpperCase() + en.name().toLowerCase().substring(1);
        } else {
            return en.toString();
        }
    }
}
