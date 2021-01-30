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
    private Number min;
    private Number max;
    private int difference;
    public NumberElement(Setting setting, int x, int y) {
        super(setting, x, y);
        this.min = (Number)setting.getMinimum();
        this.max = (Number)setting.getMaximum();
        this.difference = this.max.intValue() - this.min.intValue();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int c = ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, true, false);
        double endOffset = ((Number)this.getSetting().getValue()).floatValue() <= this.min.floatValue() ? this.getX() : this.getX() + ((float)100) * this.partialMultiplier();
        //int offset = getOffset(this.getSetting());
        RenderUtil.drawRect((int)endOffset, this.getY(), this.getX()+100, this.getY()+15, c);
        RenderUtil.drawOutlinedRectangle(this.getX(), this.getY(), this.getX()+100, this.getY()+15, 0xff2e2e2e);
        dragSetting(mouseX, mouseY);
        Number currentVal = (Number) this.getSetting().getValue();
        int startColor = ClickGUI.INSTANCE.getColorHovering(this.getY(), this.getX(), this.getY(), mouseX, mouseY, false, false);
        int endColor = ClickGUI.INSTANCE.getColorHovering(this.getY()+15, this.getX(), this.getY(), mouseX, mouseY, false, false);
        RenderUtil.drawGradientRect(this.getX(), this.getY()+1, (int)endOffset, this.getY()+15, startColor, endColor);
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
        float percent = ((float)mouseX - this.getX()) / ((float)100);
        if (this.getSetting().getValue() instanceof Double) {
            double result = (Double)this.getSetting().getMinimum() + (double)((float)this.difference * percent);
            this.getSetting().setValue((double)Math.round(10.0 * result) / 10.0);
        } else if (this.getSetting().getValue() instanceof Float) {
            float result = ((Float)this.getSetting().getMinimum()).floatValue() + (float)this.difference * percent;
            this.getSetting().setValue(Float.valueOf((float)Math.round(10.0f * result) / 10.0f));
        } else if (this.getSetting().getValue() instanceof Integer) {
            this.getSetting().setValue((Integer)this.getSetting().getMinimum() + (int)((float)this.difference * percent));
        }
        /*double diff = Math.min(100, Math.max(0, mouseX - this.getX()));
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
        }*/
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

    private float middle() {
        return this.max.floatValue() - this.min.floatValue();
    }

    private float part() {
        return ((Number)this.getSetting().getValue()).floatValue() - this.min.floatValue();
    }

    private float partialMultiplier() {
        return this.part() / this.middle();
    }
}
