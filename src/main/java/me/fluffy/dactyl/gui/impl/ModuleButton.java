package me.fluffy.dactyl.gui.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.gui.ClickGUI;
import me.fluffy.dactyl.gui.impl.setting.*;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.ClickGuiModule;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleButton {
    private final Module module;
    private final ModulePanel modulePanel;
    private int x, y;
    private boolean extended;
    private final ArrayList<SettingElement> settingElements = new ArrayList<>();
    public ModuleButton(ModulePanel modulePanel, Module module, int x, int y) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.extended = false;
        this.modulePanel = modulePanel;
        setupSettings();
    }

    public void drawScreen(int drawX, int drawY, float partialTicks) {
        drawButton(drawX, drawY);
        drawSettings(drawX, drawY, partialTicks);
    }

    public void drawSettings(int drawX, int drawY, float partialTicks) {
        if(this.extended) {
            int bottomY = this.y+15;
            for(SettingElement element : settingElements) {
                if(element.getSetting().isVisible()) {
                    element.setY(bottomY);
                    element.drawScreen(drawX, drawY, partialTicks);
                    bottomY+=15;
                }
            }
            for(ModuleButton moduleButton : this.modulePanel.getModuleButtons()) {
                if(moduleButton.getY() + 15 > this.getY() + 15) {
                    moduleButton.setY(bottomY);
                    bottomY+=15;
                }
            }
            RenderUtil.drawOutlinedGradientRectangleZ(x, this.y+15, x+100,this.y+15+getSettingsSpace(), 500.0D);
            //RenderUtil.drawOutlinedRectangle(x, this.y+15, x+100, this.y+15+getSettingsSpace(), 0xff004fcf, 300.0D);
        }
    }

    public void onKeyPressed(char eventChar, int key) {
        for(SettingElement settingElement : settingElements) {
            if(settingElement.isVisible()) {
                settingElement.onKeyPressed(eventChar, key);
            }
        }
    }

    public ArrayList<SettingElement> getSettingElements() {
        return this.settingElements;
    }

    public void drawButton(int mouseX, int mouseY) {
        int startColor = ClickGUI.INSTANCE.getColorHovering(this.y, this.x, this.y, mouseX, mouseY, !(module.isEnabled() || module.isAlwaysListening()), false);
        int endColor = ClickGUI.INSTANCE.getColorHovering(this.y+15, this.x, this.y, mouseX, mouseY, !(module.isEnabled() || module.isAlwaysListening()), false);
        //int color = ClickGUI.INSTANCE.getHoverColor(this, mouseX, mouseY, !(module.isEnabled() || module.isAlwaysListening()), false);
        //int startColor = Colors.INSTANCE.changeAlpha(Colors.INSTANCE.getColor(y, false), 170);
        //int endColor = Colors.INSTANCE.changeAlpha(Colors.INSTANCE.getColor(y+15, false), 170);
        RenderUtil.drawGradientRect(x, y, x+100, y+15, startColor, endColor);
        //RenderUtil.drawRect(x, y, x+100, y+15, color);
        RenderUtil.drawOutlinedRectangle(x, y, x+100, y+15, 0xff2e2e2e);
        Dactyl.fontUtil.drawString(module.getName(), x+4, y + (15 - 8) / 2, (module.isEnabled() || module.isAlwaysListening()) ? 0xffffffff : 0xff969696);
    }

    public void mouseClicked(int mouseButton, int mouseX, int mouseY) {
        if(isHovering(mouseX, mouseY) && mouseButton == 0 && !module.isAlwaysListening()) {
            module.toggle();
        }
        if(isHovering(mouseX, mouseY) && mouseButton == 1) {
            this.setExtended(!this.extended);
            for(ModuleButton button : this.modulePanel.getModuleButtons()) {
                if(button.y + 15 > y + 15) {
                    if(this.extended) {
                        button.y += this.getSettingsSpace();
                    } else {
                        button.y -= this.getSettingsSpace();
                    }
                }
                for(SettingElement s : button.settingElements) {
                    if(s.getY() + 15 > y + 15 && s.getSetting().isVisible()) {
                        if(this.extended) {
                            s.setY(s.getY() + this.getSettingsSpace());
                        } else {
                            s.setY(s.getY() - this.getSettingsSpace());
                        }
                    }
                }
            }
        }
        for(SettingElement settingElement : settingElements) {
            if(settingElement.isVisible() && settingElement.getSetting().isVisible()) {
                settingElement.mouseClicked(mouseButton, mouseX, mouseY);
            }
        }
    }

    private void setupSettings() {
        if(this.module.getSettingsList() != null) {
            for(Setting setting : this.module.getSettingsList()) {
                if(setting.getValue() instanceof Bind) {
                    settingElements.add(new BindElement(setting, this.x, this.y));
                } else if(setting.getValue() instanceof String) {
                    settingElements.add(new StringElement(setting, this.x, this.y));
                } else if(setting.getValue() instanceof Enum) {
                    settingElements.add(new EnumElement(setting, this.x, this.y));
                } else if(setting.getValue() instanceof Boolean) {
                    settingElements.add(new BooleanElement(setting, this.x, this.y));
                } else if(setting.getValue() instanceof Float || setting.getValue() instanceof Double || setting.getValue() instanceof Integer) {
                    settingElements.add(new NumberElement(setting, this.x, this.y));
                }
                //settingElements.add(new SettingElement(setting, this.x, this.y));
            }
        }
    }

    public int getSettingsSpace() {
        List<SettingElement> sets = new ArrayList<SettingElement>();
        sets.addAll(settingElements.stream().filter(setting -> setting.getSetting().isVisible()).collect(Collectors.toList()));
        return sets.size()*15;
    }

    private void setExtended(boolean extended) {
        this.extended = extended;
        if(this.extended) {
            int bottomY = 15;
            for(SettingElement settingElement : settingElements) {
                settingElement.setX(x);
                settingElement.setY(y+bottomY);
                settingElement.setVisible(true);
                bottomY+=15;
            }
        } else {
            for(SettingElement settingElement : settingElements) {
                settingElement.setVisible(false);
            }
        }
    }

    public Module getModule() {
        return this.module;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x+100 && mouseY >= y && mouseY <= y + 15;
    }
}
