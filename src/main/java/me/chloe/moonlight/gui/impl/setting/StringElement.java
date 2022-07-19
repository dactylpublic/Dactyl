package me.chloe.moonlight.gui.impl.setting;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.TimeUtil;
import me.chloe.moonlight.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class StringElement extends SettingElement {
    public boolean typing;
    public String typedSoFar = "";
    TimeUtil timer = new TimeUtil();
    public StringElement(Setting setting, int x, int y) {
        super(setting, x, y);
        typing = false;
        timer.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground(mouseX, mouseY, false);
        //GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if(!typing) {
            StringBuilder renderCharacters = new StringBuilder();
            char[] arrayChars = ((String)this.getSetting().getValue()).toCharArray();
            int off = 0;
            for(char c : arrayChars) {
                off+= Moonlight.fontUtil.getStringWidth(Character.toString(c));
                if(!(off >= 95)) {
                    renderCharacters.append(Character.toString(c));
                }
            }
            Moonlight.fontUtil.drawString(TextFormatting.GRAY + String.valueOf(renderCharacters.toString()), this.getX() + 4, this.getY() + (15 - 8) / 2, 0xffffffff);
        } else {
            Moonlight.fontUtil.drawString(TextFormatting.GRAY + String.valueOf(typedSoFar), this.getX() + 4, this.getY() + (15 - 8) / 2, 0xffffffff);
            if(!timer.hasPassed(500)) {
                Moonlight.fontUtil.drawString("_", (int) (this.getX() + 4 + Moonlight.fontUtil.getStringWidth(typedSoFar)), (int) (this.getY() + 3), 0xffffffff);
            } else {
                if(timer.hasPassed(1000)) {
                    timer.reset();
                }
            }
        }
        GlStateManager.resetColor();
    }

    @Override
    public void mouseClicked(int mouseButton, int mouseX, int mouseY) {
        if (isHovering(mouseX, mouseY)) {
            if(!typing) {
                typing = true;
                typedSoFar = (String) this.getSetting().getValue();
            } else {
                getSetting().setValue(typedSoFar);
                typedSoFar = (String) getSetting().getValue();
                typing = false;
            }
        }
    }

    @Override
    public void onKeyPressed(char eventChar, int key) {
        if(typing) {
            if(key == Keyboard.KEY_BACK) {
                try {
                    if(!typedSoFar.equalsIgnoreCase("")) {
                        typedSoFar = removeLastCharacter(typedSoFar);
                    }
                } catch(Exception excep) {
                    excep.printStackTrace();
                }
            }
            if(key != Keyboard.KEY_RETURN && key != Keyboard.KEY_ESCAPE && key != Keyboard.KEY_BACK) {
                if(key != Keyboard.KEY_LSHIFT) {
                    if(key != 0 && eventChar != 0) {
                        typedSoFar += Character.toString(eventChar);
                    }
                }
            } else {
                if(key != Keyboard.KEY_BACK) {
                    getSetting().setValue(typedSoFar);
                    typedSoFar = (String) getSetting().getValue();
                    typing = false;
                }
            }
        }
    }


    public static String removeLastCharacter(String str) {
        String result = null;
        if ((str != null) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 1);
        }
        return result;
    }
}
