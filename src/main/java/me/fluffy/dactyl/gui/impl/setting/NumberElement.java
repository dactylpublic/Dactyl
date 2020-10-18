package me.fluffy.dactyl.gui.impl.setting;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.gui.ClickGUI;
import me.fluffy.dactyl.gui.impl.ModuleButton;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberElement extends SettingElement {
    public NumberElement(Setting setting, int x, int y) {
        super(setting, x, y);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //this.drawDefaultBackground(mouseX, mouseY, false);
        int c = ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, true, false);
        RenderUtil.drawRect(this.getX()+getOffset(this.getSetting()), this.getY(), this.getX()+100, this.getY()+15, c);
        RenderUtil.drawOutlinedRectange(this.getX(), this.getY(), this.getX()+100, this.getY()+15, 0xff2e2e2e);
        dragSetting(mouseX, mouseY);
        //RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + 100, this.getY() + 15, ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, true, false));
        Number currentVal = (Number) this.getSetting().getValue();
        if(!(currentVal.doubleValue() >= 0.0 && currentVal.doubleValue() <= 0.1)) {
            RenderUtil.drawRect(this.getX(), this.getY(), (int) (this.getX() + getOffset(this.getSetting())), this.getY() + 15, ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, false, false));
        }
        Dactyl.fontUtil.drawString(getSetting().getName() + " " + TextFormatting.GRAY + ((this.getSetting().getValue() instanceof Float) ? String.format("%.01f", (Float) this.getSetting().getValue()) : String.valueOf(Double.valueOf(((Number)this.getSetting().getValue()).doubleValue()))), this.getX() + 4, this.getY() + (15 - 8) / 2, 0xffffffff);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovering(mouseX, mouseY))
            setSettingFromX(mouseX);
    }

    public void dragSetting(int mouseX, int mouseY) {
        if (isHovering(mouseX, mouseY) && Mouse.isButtonDown(0))
            setSettingFromX(mouseX);
    }

    public void setSettingFromX(int mouseX) {
        double diff = Math.min(100, Math.max(0, mouseX - this.getX()));
        double max = 0;
        double min = 0;
        // floats are broken atm
        try {
            if (this.getSetting().getValue() instanceof Float) {
                max = Double.valueOf(this.getSetting().getMaximum().toString()).doubleValue();
                min = Double.valueOf(this.getSetting().getMinimum().toString()).doubleValue();
            } else if (this.getSetting().getValue() instanceof Double) {
                max = (Double) this.getSetting().getMaximum();
                min = (Double) this.getSetting().getMinimum();
            } else if (this.getSetting().getValue() instanceof Integer) {
                max = (Integer) this.getSetting().getMaximum();
                min = (Integer) this.getSetting().getMinimum();
            }
            BigDecimal bd = BigDecimal.valueOf((diff / 100) * (max - min) + min);
            bd = bd.setScale(1, RoundingMode.HALF_UP);
            if (this.getSetting().getValue() instanceof Float) {
                this.getSetting().setValue(bd.floatValue());
            } else if (this.getSetting().getValue() instanceof Double) {
                this.getSetting().setValue(bd.doubleValue());
            } else if (this.getSetting().getValue() instanceof Integer) {
                this.getSetting().setValue(bd.intValue());
            }
        } catch(ClassCastException exception) {
            // cba to fix and its not that big of an issue :^)
        }
    }

    public int getOffset(Setting val) {
        int minimumX = this.getX();
        int maximumX = this.getX() + 100;
        if (val.getMaximum() == null) {
            return minimumX;
        }
        Number setVal = (Number) val.getValue();
        Number maxVal = (Number) val.getMaximum();
        return (int) ((maximumX - minimumX) * (setVal.floatValue() / maxVal.floatValue()));
    }
}
