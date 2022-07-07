package me.chloe.dactyl.module.impl.client;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.HashMap;

public class Colors extends Module {
    public Setting<ColorMode> colorModeSetting = new Setting<ColorMode>("Mode", ColorMode.RAINBOW);

    // rainbow
    public Setting<Boolean> rollingRainbow = new Setting<Boolean>("RollingRainbow", true, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowSpeed = new Setting<Integer>("RainbowSpeed", 75, 1, 100, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowSaturation = new Setting<Integer>("Saturation", 175, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowBrightness = new Setting<Integer>("Brightness", 255, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowFactor = new Setting<Integer>("Factor", 2, 1, 10, vis->colorModeSetting.getValue() == ColorMode.RAINBOW&&rollingRainbow.getValue());
    public Setting<Boolean> reverseMap = new Setting<Boolean>("ReverseRainbow", true, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);

    // rgb
    public Setting<Integer> red = new Setting<Integer>("Red", 255, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RGB);
    public Setting<Integer> green = new Setting<Integer>("Green", 255, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RGB);
    public Setting<Integer> blue = new Setting<Integer>("Blue", 255, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RGB);
    public Setting<Integer> alpha = new Setting<Integer>("Alpha", 255, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RGB);

    //public Setting<Integer> rainbowSaturation = new Setting<Integer>("Saturation", 175, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);


    public float hue;

    public HashMap<Integer, Integer> colorX = new HashMap<>();
    public HashMap<Integer, Integer> colorY = new HashMap<>();


    public static Colors INSTANCE;
    public Colors() {
        super("Colors", Category.CLIENT);
        INSTANCE = this;
        this.setAlwaysListening(true);
    }

    @Override
    public void onClientUpdate() {
        if(colorModeSetting.getValue() == ColorMode.RAINBOW) {
            updateRainbow();
        }
    }

    public int getColor(int offset, boolean isX) {
        if(colorModeSetting.getValue() == ColorMode.RAINBOW) {
            if(rollingRainbow.getValue()) {
                return getMappedColor(offset, isX, false);
            } else {
                return getMappedColor(1, false, false);
            }
        } else if(colorModeSetting.getValue() == ColorMode.RGB) {
            return new Color(red.getValue(), green.getValue(), blue.getValue()).getRGB();
        }
        return -1;
    }

    public Color getCurrentColor() {
        if (colorModeSetting.getValue() == ColorMode.RAINBOW) {
            return Color.getHSBColor(this.hue, rainbowSaturation.getValue() / 255.0F, rainbowBrightness.getValue() / 255.0F);
        } else {
            return new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
        }
    }

    public int getColorDarker(int offset, boolean isX) {
        if(colorModeSetting.getValue() == ColorMode.RAINBOW) {
            if(rollingRainbow.getValue()) {
                return getMappedColor(offset, isX, true);
            } else {
                return getMappedColor(1, false, true);
            }
        } else if(colorModeSetting.getValue() == ColorMode.RGB) {
            return new Color(red.getValue(), green.getValue(), blue.getValue()).darker().getRGB();
        }
        return -1;
    }

    private int getMappedColor(int offset, boolean isX, boolean darker) {
        if(isX) {
            if(colorX.get(offset) != null) {
                if(darker) {
                    return convertHex(colorX.get(offset)).darker().getRGB();
                } else {
                    return colorX.get(offset);
                }
            }
        } else {
            if(colorY.get(offset) != null) {
                if(darker) {
                    return convertHex(colorY.get(offset)).darker().getRGB();
                } else {
                    return colorY.get(offset);
                }
            }
        }

        return -1;
    }

    public Color convertHex(int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return new Color(r, g, b, 255);
    }

    public boolean isRolling() {
        return (rollingRainbow.getValue() && (colorModeSetting.getValue() == ColorMode.RAINBOW));
    }

    private void updateRainbow() {
        ScaledResolution res = getRes();
        int zoomSpeed = 101 - rainbowSpeed.getValue();
        hue = (System.currentTimeMillis() % (360 * zoomSpeed)) / (360f * zoomSpeed);
        float tempHue = hue;
        if(reverseMap.getValue()) {
            for (int i = res.getScaledHeight() - 1; i >= 0; i--) {
                colorY.put(i, Color.HSBtoRGB(tempHue, rainbowSaturation.getValue() / 255f, rainbowBrightness.getValue() / 255f));
                tempHue += ((float) rainbowFactor.getValue()) / res.getScaledHeight();
            }
            float tempHue1 = hue;
            for (int i = res.getScaledWidth() - 1; i >= 0; i--) {
                colorX.put(i, Color.HSBtoRGB(tempHue1, rainbowSaturation.getValue() / 255f, rainbowBrightness.getValue() / 255f));
                tempHue1 += ((float) rainbowFactor.getValue()) / res.getScaledWidth();
            }
        } else {
            for (int i = 0; i <= res.getScaledHeight(); i++) {
                colorY.put(i, Color.HSBtoRGB(tempHue, rainbowSaturation.getValue() / 255f, rainbowBrightness.getValue() / 255f));
                tempHue += ((float) rainbowFactor.getValue()) / res.getScaledHeight();
            }
            float tempHue1 = hue;
            for (int i = 0; i <= res.getScaledWidth(); i++) {
                colorX.put(i, Color.HSBtoRGB(tempHue1, rainbowSaturation.getValue() / 255f, rainbowBrightness.getValue() / 255f));
                tempHue1 += ((float) rainbowFactor.getValue()) / res.getScaledWidth();
            }
        }
    }

    public int changeAlpha(int original, int newAlpha) {
        original = original & 0x00ffffff;
        return (newAlpha << 24) | original;
    }



    private enum ColorMode {
        RAINBOW,
        RGB
    }
}
