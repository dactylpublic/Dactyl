package me.fluffy.dactyl.gui;

import me.fluffy.dactyl.gui.impl.ModuleButton;
import me.fluffy.dactyl.gui.impl.ModulePanel;
import me.fluffy.dactyl.gui.impl.setting.SettingElement;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

public class ClickGUI extends GuiScreen {

    private ArrayList<ModulePanel> modulePanels = new ArrayList<>();

    public static ClickGUI INSTANCE;

    public ClickGUI() {
        int x = 1;
        for(Module.Category category : Module.Category.values()) {
            modulePanels.add(new ModulePanel(category, x, 1));
            x+=105;
        }
        INSTANCE = this;
    }

    public int getColorHovering(int outY, int targetX, int targetY, int mouseX, int mouseY, boolean idle, boolean fullalpha) {
        boolean hovered = false;
        if(mouseX >= targetX && mouseX <= targetX+100 && mouseY >= targetY && mouseY <= targetY+15) hovered = true;
        if(hovered) {
            if(fullalpha) {
                if(idle) {
                    return 0xff3b3b3b;
                } else {
                    return Colors.INSTANCE.getColor(outY, false);
                }
            } else {
                if(idle) {
                    return 0x573b3b3b;
                } else {
                    return Colors.INSTANCE.changeAlpha(Colors.INSTANCE.getColorDarker(outY, false), 150);
                }
            }
        } else {
            if(fullalpha) {
                if(idle) {
                    return 0xff4f4f4f;
                } else {
                    return Colors.INSTANCE.getColor(outY, false);
                }
            } else {
                if(idle) {
                    return 0x574f4f4f;
                } else {
                    return Colors.INSTANCE.changeAlpha(Colors.INSTANCE.getColor(outY, false), 150);
                }
            }
        }
    }

    public int getHoverColor(Object guiElement, int mouseX, int mouseY, boolean idle, boolean fullalpha) {
        if(guiElement instanceof ModuleButton) {
            ModuleButton moduleButton = (ModuleButton)guiElement;
            return getColorOld(fullalpha, idle, moduleButton.isHovering(mouseX, mouseY));
        } else if(guiElement instanceof SettingElement) {
            SettingElement settingElement = (SettingElement)guiElement;
            return getColorOld(fullalpha, idle, settingElement.isHovering(mouseX, mouseY));
        }
        return -1;
    }

    public int getColorOld(boolean fullalpha, boolean idle, boolean hovering) {
        if(hovering) {
            if(fullalpha) {
                if(idle) {
                    return 0xff3b3b3b;
                } else {
                    return 0xff003da1;
                }
            } else {
                if(idle) {
                    return 0x573b3b3b;
                } else {
                    return 0x57003da1;
                }
            }
        } else {
            if(fullalpha) {
                if(idle) {
                    return 0xff4f4f4f;
                } else {
                    return 0xff004fcf;
                }
            } else {
                if(idle) {
                    return 0x574f4f4f;
                } else {
                    return 0x57004fcf;
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.world != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -0x73EFEFF0, -0x73EFEFF0);
        }
        for(ModulePanel p : modulePanels) {
            p.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        handleNoise(mouseX, mouseY);
        for(ModulePanel modulePanel : modulePanels) {
            modulePanel.mouseClicked(mouseButton, mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for(ModulePanel modulePanel : modulePanels) {
            for(ModuleButton button : modulePanel.getModuleButtons()) {
                for(SettingElement settingElement : button.getSettingElements()) {
                    if(settingElement.isHovering(mouseX, mouseY)) {
                        settingElement.onMouseRelease(mouseX, mouseY, mouseButton);
                    }
                }
            }
        }
    }

    private void handleNoise(int mouseX, int mouseY) {
        for(ModulePanel panel : modulePanels) {
            if(mouseX >= panel.getX() && mouseX <= panel.getX() + 100 && mouseY >= panel.getY() && mouseY <= panel.getY() + 15) {
                playButtonSound();
            }
            for(ModuleButton button : panel.getModuleButtons()) {
                if(mouseX >= button.getX() && mouseX <= button.getX() + 100 && mouseY >= button.getY() && mouseY <= button.getY() + 15) {
                    playButtonSound();
                }
                for(SettingElement element : button.getSettingElements()) {
                    if(mouseX >= element.getX() && mouseX <= element.getX() + 100 && mouseY >= element.getY() && mouseY <= element.getY() + 15) {
                        playButtonSound();
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(char eventChar, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
        for(ModulePanel modulePanel : modulePanels) {
            modulePanel.onKeyPressed(eventChar, key);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    public void playButtonSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public void handleMouseInput() throws IOException {
        if(Mouse.getEventDWheel() > 0) {
            for(ModulePanel panel : modulePanels) {
                panel.setY(panel.getY()+13);
            }
        }
        if(Mouse.getEventDWheel() < 0) {
            for(ModulePanel panel : modulePanels) {
                panel.setY(panel.getY()-13);
            }
        }
        super.handleMouseInput();
    }
}
