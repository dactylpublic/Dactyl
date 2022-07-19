package me.chloe.moonlight.gui.impl.setting;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.StringUtil;
import me.chloe.moonlight.setting.Setting;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class EnumElement extends SettingElement{
    public EnumElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    private static final String[] whitelistedWords = new String[] {"AAC", "NCP"};

    @Override
    public void drawScreen(int drawX, int drawY, float partialTicks) {
        this.drawDefaultBackground(drawX, drawY, true);
        Moonlight.fontUtil.drawString(getSetting().getName() + " " + TextFormatting.GRAY + getEnumName((Enum) getSetting().getValue()), this.getX()+4, this.getY() + (15 - 8) / 2, 0xffffffff);
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
