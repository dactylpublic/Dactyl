package me.chloe.moonlight.module.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.command.impl.ConfigCommand;
import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.listener.impl.SpeedListener;
import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.util.EntityUtil;
import me.chloe.moonlight.util.MathUtil;
import me.chloe.moonlight.util.TimeUtil;
import me.chloe.moonlight.util.render.RenderUtil;
import me.chloe.moonlight.event.impl.render.PotionHUDEvent;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.util.render.font.CFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;

public class HUD extends Module {

    public Setting<Boolean> customFont = new Setting<>("CustomFont", true);
    public Setting<Boolean> renderHud = new Setting<>("RenderHUD", true);
    public Setting<Boolean> potionEffects = new Setting<>("Potions", true, v->renderHud.getValue());
    public Setting<Boolean> potionColorSync = new Setting<>("PotionCSync", false, v->renderHud.getValue());
    public Setting<Boolean> potionIcons = new Setting<>("PotionIcons", false);
    public Setting<Boolean> arrayListSetting = new Setting<>("ArrayList", true, v->renderHud.getValue());
    public Setting<Boolean> alphabetical = new Setting<>("ABC", false, v->renderHud.getValue()&&arrayListSetting.getValue());
    public Setting<Integer> arrayListUpdates = new Setting<>("ArrayUpdates", 200, 50, 1000, v->renderHud.getValue()&&arrayListSetting.getValue());
    public Setting<Integer> arrayListFactor = new Setting<>("AnimFactor", 2, 1, 10, v->renderHud.getValue()&&arrayListSetting.getValue());
    public Setting<Boolean> fps = new Setting<>("FPS", true, v->renderHud.getValue());
    public Setting<Boolean> tps = new Setting<>("TPS", true, v->renderHud.getValue());
    public Setting<Boolean> speed = new Setting<>("Speed", true, v->renderHud.getValue());
    public Setting<Boolean> ping = new Setting<>("Ping", true, v->renderHud.getValue());
    public Setting<Boolean> clock = new Setting<>("Clock", true, v->renderHud.getValue());
    public Setting<Boolean> playerCount = new Setting<>("PlayerCount", false, v->renderHud.getValue());
    public Setting<Boolean> grayColor = new Setting<>("GrayColor", false, v->renderHud.getValue());
    public Setting<Boolean> direction = new Setting<>("Direction", true, v->renderHud.getValue());
    public Setting<Boolean> coords = new Setting<>("Coords", true, v->renderHud.getValue());
    public Setting<Boolean> netherCoords = new Setting<>("NetherCoords", true, v->renderHud.getValue() && coords.getValue());
    //public Setting<CoordsSide> coordsSideSetting = new Setting<CoordsSide>("Side", CoordsSide.LEFT, v->renderHud.getValue() && coords.getValue());
    public Setting<Boolean> lagNotifier = new Setting<>("LagNotifier", true, v->renderHud.getValue());
    public Setting<Boolean> armorHud = new Setting<>("ArmorHUD", true, v->renderHud.getValue());
    public Setting<Boolean> totemCount = new Setting<>("TotemCount", true, v->renderHud.getValue());
    public Setting<Rendering> renderingSetting = new Setting<>("Rendering", Rendering.UP, v->renderHud.getValue());

    public Setting<WatermarkType> watermarkTypeSetting = new Setting<>("Logo", WatermarkType.MOONLIGHT_IE, v->renderHud.getValue());
    public Setting<Boolean> skeetWatermark = new Setting<>("SkeetWatermark", false, v->renderHud.getValue() && watermarkTypeSetting.getValue() != WatermarkType.NONE);
    public Setting<String> customWatermark = new Setting<>("CustomWatermark", "Moonlight.ie", vis->renderHud.getValue() && watermarkTypeSetting.getValue() == WatermarkType.CUSTOM);
    public Setting<Integer> waterMarkOffset = new Setting<>("LogoOffset", 0, 0, 100, v->renderHud.getValue() && watermarkTypeSetting.getValue() != WatermarkType.NONE);
    public Setting<Boolean> gradientLogo = new Setting<>("LogoGradient", false, v->renderHud.getValue());

    public Setting<Boolean> welcomer = new Setting<>("Welcomer", true, v->renderHud.getValue());
    public Setting<String> customWelcomer = new Setting<>("CustomWelcomerr", "Hello <p>, welcome to Moonlight.ie", v->renderHud.getValue()&&welcomer.getValue());
    public Setting<Boolean> welcomerGradient = new Setting<>("WelcomeGradient", true, v->renderHud.getValue()&&welcomer.getValue());

    public Setting<Boolean> currentConfig = new Setting<>("CurrentConfig", false, v->renderHud.getValue());
    public Setting<Integer> currentConfigOffset = new Setting<>("ConfigOffset", 10, 0, 100, v->renderHud.getValue() && currentConfig.getValue());
    public Setting<Boolean> currentConfigGradient = new Setting<>("ConfigGradient", false, v->renderHud.getValue()&&currentConfig.getValue());

    public Setting<Boolean> shadow = new Setting<>("Shadow", true, v->renderHud.getValue());

    public Setting<String> commandPrefix = new Setting<>("CommandPrefix", "-");


    public static HUD INSTANCE;
    public HUD() {
        super("HUD", Category.CLIENT, true);
        INSTANCE = this;
    }

    public static final ArrayList<ArrayListElement> arrayListElements = new ArrayList<>();
    public static final TimeUtil arrayTimer = new TimeUtil();

    private long serverLastUpdated = System.currentTimeMillis();
    private static final ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);

    public float hue;

    public HashMap<Integer, Integer> colorX = new HashMap<>();
    public HashMap<Integer, Integer> colorY = new HashMap<>();

    @Override
    public void onClientUpdate() {
        if(mc.player == null) {
            return;
        }
        if(skeetWatermark.getValue() && watermarkTypeSetting.getValue() != WatermarkType.NONE && renderHud.getValue()) {
            updateSkeetRainbow();
        }
        if(arrayTimer.hasPassed(arrayListUpdates.getValue())) {
            updateArrayList();
            sortModules();
        }
    }

    private void updateArrayList() {
        for(Module module : Moonlight.moduleManager.getModules()) {
            if(module.isEnabled() && !((module.isHidden() || module.isAlwaysListening() || module.getCategory().equals(Category.CLIENT)))) {
                if(!arrayListContainsModule(module)) {
                    ArrayListElement element = new ArrayListElement(module);
                    element.state = AnimationState.OPEN;
                    arrayListElements.add(element);
                }
            } else {
                if(arrayListContainsModule(module)) {
                    ArrayListElement element = getArrayListElement(module);
                    if(element != null) {
                        element.state = AnimationState.CLOSE;
                    }
                }
            }
            if(arrayListContainsModule(module) && (module.isHidden() || module.isAlwaysListening() || module.getCategory().equals(Category.CLIENT))) {
                arrayListElements.remove(getArrayListElement(module));
            }
        }
        sortModules();
    }

    private void sortModules() {
        Comparator<ArrayListElement> comparator = (first, second) ->
        {
            if(!alphabetical.getValue()) {
                String firstName = first.module.getDisplayName() + (first.module.getModuleInfo().length() > 0 ? " [" + first.module.getModuleInfo() + "]" : "");
                String secondName = second.module.getDisplayName() + (second.module.getModuleInfo().length() > 0 ? " [" + second.module.getModuleInfo() + "]" : "");
                float dif = Moonlight.fontUtil.getStringWidth(secondName) - Moonlight.fontUtil.getStringWidth(firstName);
                return dif != 0 ? (int) dif : secondName.compareTo(firstName);
            } else {
                String firstName = first.module.getDisplayName() + (first.module.getModuleInfo().length() > 0 ? " [" + first.module.getModuleInfo() + "]" : "");
                String secondName = second.module.getDisplayName() + (second.module.getModuleInfo().length() > 0 ? " [" + second.module.getModuleInfo() + "]" : "");
                return secondName.compareTo(firstName);
            }
        };
        arrayListElements.sort(comparator);
    }

    @Override
    public void onScreen() {
        if(mc.player == null) {
            return;
        }
        if(renderHud.getValue()) {
            doArrayList();
            doWatermark();
            doWelcomer();
            doCurrentConfig();
            doOtherRender();
            renderDirAndCoords();
            renderArmorHUD();
            renderTotemHUD();
        }
    }

    private void doArrayList() {
        if(arrayListSetting.getValue()) {
            if(renderingSetting.getValue() == Rendering.UP) {
                int textY = 1;
                if(!alphabetical.getValue()) {
                    for (ArrayListElement element : arrayListElements) {
                        String moduleInfoString = TextFormatting.GRAY + " [" + TextFormatting.WHITE + element.module.getModuleInfo() + TextFormatting.GRAY + "]";
                        String renderString = element.module.getDisplayName() + (element.module.hasModuleInfo() ? moduleInfoString : "");
                        int currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2;

                        if (element.state == AnimationState.OPEN) {
                            element.ticks = element.ticks - arrayListFactor.getValue();
                            if (element.ticks <= 0) {
                                element.state = AnimationState.NONE;
                            } else {
                                currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + element.ticks;
                            }
                        } else if (element.state == AnimationState.CLOSE) {
                            if (element.ticks <= 0) {
                                element.ticks = 1;
                            }
                            element.ticks += arrayListFactor.getValue();
                            currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + element.ticks;
                        }
                        if (shadow.getValue()) {
                            Moonlight.fontUtil.drawStringWithShadow(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                        } else {
                            Moonlight.fontUtil.drawString(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                        }
                        textY += 10;
                    }
                } else {
                    for (int i = arrayListElements.size() - 1; i >= 0; i--) {
                        String moduleInfoString = TextFormatting.GRAY + " [" + TextFormatting.WHITE + arrayListElements.get(i).module.getModuleInfo() + TextFormatting.GRAY + "]";
                        String renderString = arrayListElements.get(i).module.getDisplayName() + (arrayListElements.get(i).module.hasModuleInfo() ? moduleInfoString : "");
                        int currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2;

                        if (arrayListElements.get(i).state == AnimationState.OPEN) {
                            arrayListElements.get(i).ticks = arrayListElements.get(i).ticks - arrayListFactor.getValue();
                            if (arrayListElements.get(i).ticks <= 0) {
                                arrayListElements.get(i).state = AnimationState.NONE;
                            } else {
                                currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                            }
                        } else if (arrayListElements.get(i).state == AnimationState.CLOSE) {
                            if (arrayListElements.get(i).ticks <= 0) {
                                arrayListElements.get(i).ticks = 1;
                            }
                            arrayListElements.get(i).ticks += arrayListFactor.getValue();
                            currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                        }
                        if (shadow.getValue()) {
                            Moonlight.fontUtil.drawStringWithShadow(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                        } else {
                            Moonlight.fontUtil.drawString(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                        }
                        textY += 10;
                    }
                }
                arrayListElements.removeIf(element -> element.state == AnimationState.CLOSE && element.ticks >= 50);
            } else {
                int textY = RenderUtil.getScreenHeight()-(arrayListElements.size()*10)-1;
                if(mc.currentScreen instanceof GuiChat) textY = textY-15;
                for (int i = arrayListElements.size() - 1; i >= 0; i--) {
                    if(arrayListElements.get(i) == null) {
                        return;
                    }
                    String moduleInfoString = TextFormatting.GRAY + " [" + TextFormatting.WHITE + arrayListElements.get(i).module.getModuleInfo() + TextFormatting.GRAY + "]";
                    String renderString = arrayListElements.get(i).module.getDisplayName() + (arrayListElements.get(i).module.hasModuleInfo() ? moduleInfoString : "");
                    int currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2;

                    if(arrayListElements.get(i).state == AnimationState.OPEN) {
                        arrayListElements.get(i).ticks = arrayListElements.get(i).ticks-arrayListFactor.getValue();
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).state = AnimationState.NONE;
                        } else {
                            currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                        }
                    } else if(arrayListElements.get(i).state == AnimationState.CLOSE) {
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).ticks = 1;
                        }
                        arrayListElements.get(i).ticks+=arrayListFactor.getValue();
                        currentX = (int) (RenderUtil.getScreenWidth() - Moonlight.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                    }
                    if (shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Moonlight.fontUtil.drawString(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                    textY+=10;
                }
                arrayListElements.removeIf(element-> element.state == AnimationState.CLOSE && element.ticks >= 50);
            }
        }
    }

    private void doOtherRender() {
        Calendar rightNow = Calendar.getInstance();
        ArrayList<TextElement> pots = new ArrayList<>();
        ArrayList<TextElement> normal = new ArrayList<>();

        if(potionEffects.getValue()) {
            Collection<PotionEffect> effects = mc.player.getActivePotionEffects();
            if (!effects.isEmpty()) {
                for (PotionEffect effect : effects) {
                    if (effect != null) {
                        Potion potion = effect.getPotion();
                        String name = I18n.format(potion.getName());

                        switch (effect.getAmplifier()) {
                            case 1: {
                                name += " 2";
                                break;
                            }
                            case 2: {
                                name += " 3";
                                break;
                            }
                            case 3: {
                                name += " 4";
                                break;
                            }
                        }

                        name += TextFormatting.WHITE + " "+Potion.getPotionDurationString(effect, 1.0F);
                        pots.add(new TextElement(name, potion.getLiquidColor(), !potionColorSync.getValue()));
                        pots.sort(sortByLength);
                    }
                }
            }
        }

        if(fps.getValue()) {
            if(!grayColor.getValue()) {
                normal.add(new TextElement(TextFormatting.RESET + "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS(), 0xffffffff, false));
            } else {
                normal.add(new TextElement(TextFormatting.GRAY + "FPS " + TextFormatting.WHITE + Minecraft.getDebugFPS(), 0xffffffff, false));
            }
        }

        if(tps.getValue()) {
            float tick = Moonlight.INSTANCE.getTickRateManager().getTickRate();
            float rounded = (float) ((float)Math.round(tick * 100.0) / 100.0);
            if(!grayColor.getValue()) {
                normal.add(new TextElement(TextFormatting.RESET + "TPS " + TextFormatting.WHITE + rounded, 0xffffffff, false));
            } else {
                normal.add(new TextElement(TextFormatting.GRAY + "TPS " + TextFormatting.WHITE + rounded, 0xffffffff, false));
            }
        }

        if(ping.getValue()) {
            int ms = EntityUtil.getPing(mc.player);
            String msText = ChatFormatting.RESET + "Ping " + ChatFormatting.WHITE + ms + "ms";
            if(!grayColor.getValue()) {
                normal.add(new TextElement(msText, 0xffffffff, false));
            } else {
                String msGrayText = ChatFormatting.GRAY + "Ping " + ChatFormatting.WHITE + ms + "ms";
                normal.add(new TextElement(msGrayText, 0xffffffff, false));
            }
        }

        if(clock.getValue()) {
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            int mins = rightNow.get(Calendar.MINUTE);

            boolean isAfternoon = hour >= 12;
            if(isAfternoon && (hour-12 == 0))
                hour = 24;

            String hourString = isAfternoon ? String.valueOf(hour-12) : String.valueOf(hour);
            String textTime = ChatFormatting.RESET + "Time " + ChatFormatting.WHITE + hourString +":"+(mins < 10 ? "0" : "") + mins + (isAfternoon ? "pm" : "am");
            if(!grayColor.getValue()) {
                normal.add(new TextElement(textTime, 0xffffffff, false));
            } else {
                String textGrayTime = ChatFormatting.GRAY + "Time " + ChatFormatting.WHITE + hourString +":"+(mins < 10 ? "0" : "") + mins + (isAfternoon ? "pm" : "am");
                normal.add(new TextElement(textGrayTime, 0xffffffff, false));
            }
        }

        if(playerCount.getValue()) {
            int count;
            if(mc.player == null || mc.player.connection == null || mc.isSingleplayer()) {
                count = 1;
            } else {
                count = mc.player.connection.getPlayerInfoMap().size();
            }
            if(!grayColor.getValue()) {
                normal.add(new TextElement(TextFormatting.RESET + "Players " + TextFormatting.WHITE + count, 0xffffffff, false));
            } else {
                normal.add(new TextElement(TextFormatting.GRAY + "Players " + TextFormatting.WHITE + count, 0xffffffff, false));
            }
        }

        if(speed.getValue()) {
            if(!grayColor.getValue()) {
                normal.add(new TextElement(TextFormatting.RESET + "Speed " + TextFormatting.WHITE + String.format("%,.2f", SpeedListener.INSTANCE.getSpeedKpH()) + " km/h", 0xffffffff, false));
            } else {
                normal.add(new TextElement(TextFormatting.GRAY + "Speed " + TextFormatting.WHITE + String.format("%,.2f", SpeedListener.INSTANCE.getSpeedKpH()) + " km/h", 0xffffffff, false));
            }
        }


        if(lagNotifier.getValue()) {
            String lag = TextFormatting.GRAY + "Server is not responding " + timeDifference() + "s";
            if ((1000L <= System.currentTimeMillis() - serverLastUpdated) && !(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat))) {
                int divider = getScale();
                int lagX = (int) (mc.displayWidth / divider / 2 - Moonlight.fontUtil.getStringWidth(lag) / 2);
                int lagY = 10;
                if(shadow.getValue()) {
                    Moonlight.fontUtil.drawStringWithShadow(lag, lagX, lagY, 0xffffffff);
                } else {
                    Moonlight.fontUtil.drawString(lag, lagX, lagY, 0xffffffff);
                }
            }
        }
        pots.sort(sortByLength);
        normal.sort(sortByLength);
        ArrayList<TextElement> text = new ArrayList<>();
        text.addAll(pots);
        text.addAll(normal);

        if(renderingSetting.getValue() == Rendering.UP) {
            int textY = RenderUtil.getScreenHeight()-(text.size()*10)-1;
            if(mc.currentScreen instanceof GuiChat) textY = textY-15;
            for (int i = text.size() - 1; i >= 0; i--) {
                int thisX = (int) (RenderUtil.getScreenWidth()-(Moonlight.fontUtil.getStringWidth(text.get(i).getText()))-2);
                if(text.get(i).isPot()) {
                    if(shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(text.get(i).getText(), thisX, textY, text.get(i).getColor());
                    } else {
                        Moonlight.fontUtil.drawString(text.get(i).getText(), thisX, textY, text.get(i).getColor());
                    }
                } else {
                    if(shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(text.get(i).getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Moonlight.fontUtil.drawString(text.get(i).getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                }
                textY+=10;
            }
        } else {
            int textY = 1;
            for (TextElement s : text) {
                int thisX = (int) (RenderUtil.getScreenWidth()-(Moonlight.fontUtil.getStringWidth(s.getText()))-2);
                if(s.isPot()) {
                    if(shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(s.getText(), thisX, textY, s.getColor());
                    } else {
                        Moonlight.fontUtil.drawString(s.getText(), thisX, textY, s.getColor());
                    }
                } else {
                    if(shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(s.getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Moonlight.fontUtil.drawString(s.getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                }
                textY+=10;
            }
        }
    }

    private int getColor(int offset, boolean isX) {
        return getMappedColor(offset, isX, false);
    }

    private Color getCurrentColor() {
        return Color.getHSBColor(this.hue, 1.0f, 1F);
    }

    private int getColorDarker(int offset, boolean isX) {
        return getMappedColor(offset, isX, true);
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

    private void updateSkeetRainbow() {
        ScaledResolution res = getRes();
        int zoomSpeed = 101 - 51;
        hue = (System.currentTimeMillis() % (360f * zoomSpeed)) / (360f * zoomSpeed);
        float tempHue = hue;
        for (int i = 0; i <= res.getScaledHeight(); i++) {
            colorY.put(i, Color.HSBtoRGB(tempHue, 1.0f, 1f));
            tempHue += ((float) 5) / res.getScaledHeight();
        }
        float tempHue1 = hue;
        for (int i = 0; i <= res.getScaledWidth(); i++) {
            colorX.put(i, Color.HSBtoRGB(tempHue1, 1.0f, 1f));
            tempHue1 += ((float) 5) / res.getScaledWidth();
        }
    }

    private boolean arrayListContainsModule(Module module) {
        for(ArrayListElement arrayListElement : arrayListElements) {
            if(arrayListElement.module == module) {
                return true;
            }
        }
        return false;
    }

    private ArrayListElement getArrayListElement(Module module) {
        for(ArrayListElement arrayListElement : arrayListElements) {
            if(arrayListElement.module == module) {
                return arrayListElement;
            }
        }
        return null;
    }

    public void renderDirAndCoords()
    {
        if (direction.getValue())
        {
            String directionText;
            if (!grayColor.getValue())
                directionText = String.format("%s%s [%s%s%s]", TextFormatting.RESET, EntityUtil.getFacingWithProperCapitals(), TextFormatting.WHITE, EntityUtil.getRelativeDirection(), TextFormatting.RESET);
            else
                directionText = String.format("%s%s [%s%s%s]", TextFormatting.GRAY, EntityUtil.getFacingWithProperCapitals(), TextFormatting.WHITE, EntityUtil.getRelativeDirection(), TextFormatting.GRAY);

            int dirY = RenderUtil.getScreenHeight() - (coords.getValue() ? 10 : 0) - (mc.currentScreen instanceof GuiChat ? 15 : 0) - 10;

            if (shadow.getValue())
                Moonlight.fontUtil.drawStringWithShadow(directionText, 1, dirY, Colors.INSTANCE.getColor(dirY, false));
            else
                Moonlight.fontUtil.drawString(directionText, 1, dirY, Colors.INSTANCE.getColor(dirY, false));
        }

        if(!coords.getValue())
            return;

        String xyzText;
        if (!grayColor.getValue())
            xyzText = String.format("%sXYZ %s%.1f%s, %s%.1f%s, %s%.1f%s", TextFormatting.RESET, TextFormatting.WHITE, mc.player.posX, TextFormatting.RESET, TextFormatting.WHITE, mc.player.posY, TextFormatting.RESET, TextFormatting.WHITE, mc.player.posZ, TextFormatting.RESET);
        else
            xyzText = String.format("%sXYZ %s%.1f%s, %s%.1f%s, %s%.1f%s", TextFormatting.GRAY, TextFormatting.WHITE, mc.player.posX, TextFormatting.GRAY, TextFormatting.WHITE, mc.player.posY, TextFormatting.GRAY, TextFormatting.WHITE, mc.player.posZ, TextFormatting.GRAY);

        if(netherCoords.getValue()) {
            String netherX = mc.player.dimension != -1 ? String.format("%.1f", mc.player.posX / 8) : String.format("%.1f", mc.player.posX * 8);
            String netherZ = mc.player.dimension != -1 ? String.format("%.1f", mc.player.posZ / 8) : String.format("%.1f", mc.player.posZ * 8);

            if (!grayColor.getValue())
                xyzText += String.format(" [%s%s%s, %s%s%s]", TextFormatting.WHITE, netherX, TextFormatting.RESET, TextFormatting.WHITE, netherZ, TextFormatting.RESET);
            else
                xyzText += String.format(" [%s%s%s, %s%s%s]", TextFormatting.WHITE, netherX, TextFormatting.GRAY, TextFormatting.WHITE, netherZ, TextFormatting.GRAY);
        }

        int height = RenderUtil.getScreenHeight() - (mc.currentScreen instanceof GuiChat ? 15 : 0) - 10;
        if(shadow.getValue())
            Moonlight.fontUtil.drawStringWithShadow(xyzText, 1, height, Colors.INSTANCE.getColor(height, false));
        else
            Moonlight.fontUtil.drawString(xyzText, 1, height, Colors.INSTANCE.getColor(height, false));
    }

    public void renderTotemHUD() {
        if(!totemCount.getValue()) {
            return;
        }
        ScaledResolution renderer = new ScaledResolution(mc);
        final int width = renderer.getScaledWidth();
        final int height = renderer.getScaledHeight();
        int totems = HUD.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (HUD.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            totems += HUD.mc.player.getHeldItemOffhand().getCount();
        }
        if (totems > 0) {
            GlStateManager.enableTexture2D();
            final int i = width / 2;
            final int iteration = 0;
            final int y = height - 55 - ((HUD.mc.player.isInWater() && HUD.mc.playerController.gameIsSurvivalOrAdventure()) ? 10 : 0);
            final int x = i - 189 + 180 + 2;
            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 200.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(totemStack, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, totemStack, x, y, "");
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            Moonlight.fontUtil.drawStringWithShadow(totems + "", (int)(x + 19 - 2 - Moonlight.fontUtil.getStringWidth(totems + "")), y + 9, 16777215);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }

    public void renderArmorHUD() {
        if(!armorHud.getValue()) {
            return;
        }
        ScaledResolution renderer = new ScaledResolution(mc);
        final int width = renderer.getScaledWidth();
        final int height = renderer.getScaledHeight();
        GlStateManager.enableTexture2D();
        final int i = width / 2;
        int iteration = 0;
        final int y = height - 55 - ((HUD.mc.player.isInWater() && HUD.mc.playerController.gameIsSurvivalOrAdventure()) ? 10 : 0);
        for (final ItemStack is : HUD.mc.player.inventory.armorInventory) {
            ++iteration;
            if (is.isEmpty()) {
                continue;
            }
            final int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 200.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(is, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, is, x, y, "");
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            final String s = (is.getCount() > 1) ? (is.getCount() + "") : "";
            Moonlight.fontUtil.drawStringWithShadow(s, (int)(x + 19 - 2 - Moonlight.fontUtil.getStringWidth(s)), y + 9, 16777215);
            int dmg;
            final int itemDurability = is.getMaxDamage() - is.getItemDamage();
            final float green = (is.getMaxDamage() - (float)is.getItemDamage()) / is.getMaxDamage();
            final float red = 1.0f - green;
            dmg = 100 - (int)(red * 100.0f);
            Moonlight.fontUtil.drawStringWithShadow(dmg + "", (int)(x + 8 - Moonlight.fontUtil.getStringWidth(dmg + "") / 2), y - 11, RenderUtil.toRGBA((int)(red * 255.0f), (int)(green * 255.0f), 0));
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    private final CFontRenderer skeetFont = new CFontRenderer(new Font("Verdana", Font.PLAIN, 16), true, true);

    private void doWatermark() {
        if(watermarkTypeSetting.getValue() != WatermarkType.NONE) {
            if(skeetWatermark.getValue()) {
                String skeetWatermarkText = watermarkTypeSetting.getValue() == WatermarkType.MOONLIGHT ? "Moonlight" : "Moonlight.ie";
                if(watermarkTypeSetting.getValue() == WatermarkType.CUSTOM) {
                    skeetWatermarkText = customWatermark.getValue();
                }

                skeetWatermarkText = skeetWatermarkText + " | Build: " + Moonlight.VERSION;

                // top pixels 0xFF383838
                RenderUtil.drawOutlinedRectangle(7, waterMarkOffset.getValue()+8.0f, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+25f, 0xFF383936);

                // middle pixels 0xFF222524
                RenderUtil.drawRect(8, waterMarkOffset.getValue()+9.0f, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+10.5f, 0xFF222524);

                // bottom pixels 0XFF383838
                RenderUtil.drawRect(8, waterMarkOffset.getValue()+10.5f, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+12, 0xFF383936);

                // rainbow (3-5 pixels)
                RenderUtil.drawGradientRectHorizontal(8, waterMarkOffset.getValue()+12, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+13f, getColor(7, true), getColor((int) (Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 25), true));

                // below rainbow 0xFF100E09
                RenderUtil.drawRect(8, waterMarkOffset.getValue()+13f, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+14.5f, 0xFF100E09);

                // text rect 0xFF0C0C0C
                RenderUtil.drawRect(8, waterMarkOffset.getValue()+14.5f, Moonlight.fontUtil.getStringWidth(skeetWatermarkText) + 2, waterMarkOffset.getValue()+25f, 0xFF0C0C0C);

                // left side

                skeetFont.drawString(skeetWatermarkText, 10, waterMarkOffset.getValue()+16, 0xFFFFFFFF);

                return;
            }
            String drawingWatermark = "";
            switch(watermarkTypeSetting.getValue()) {
                case MOONLIGHT_IE:
                    drawingWatermark = "Moonlight.ie " + Moonlight.VERSION;
                    break;
                case MOONLIGHT:
                    drawingWatermark = "Moonlight " + Moonlight.VERSION;
                    break;
                case CUSTOM:
                    drawingWatermark = customWatermark.getValue().replace("<v>", Moonlight.VERSION);
                    break;
            }
            int offset = 1+waterMarkOffset.getValue();
            if(gradientLogo.getValue()) {
                char[] characters = drawingWatermark.toCharArray();
                int currentX = 1;
                for(char ch : characters) {
                    if(shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(String.valueOf(ch), currentX, offset, Colors.INSTANCE.getColor(currentX, true));
                    } else {
                        Moonlight.fontUtil.drawString(String.valueOf(ch), currentX, offset, Colors.INSTANCE.getColor(currentX, true));
                    }
                    currentX+= Moonlight.fontUtil.getStringWidth(String.valueOf(ch));
                }
            } else {
                if(shadow.getValue()) {
                    Moonlight.fontUtil.drawStringWithShadow(drawingWatermark, 1, offset, Colors.INSTANCE.getColor(1, false));
                } else {
                    Moonlight.fontUtil.drawString(drawingWatermark, 1, offset, Colors.INSTANCE.getColor(1, false));
                }
            }
        }
    }

    private void doWelcomer() {
        if(!welcomer.getValue()) {
            return;
        }
        int width = this.getRes().getScaledWidth();
        String renderText = customWelcomer.getValue().replace("<p>", mc.session.getUsername());
        if (welcomerGradient.getValue()) {
            char[] characters = renderText.toCharArray();
            int currentX = width / 2 - (int) Moonlight.fontUtil.getStringWidth(renderText) / 2 + 2;
            for (char ch : characters) {
                if (shadow.getValue()) {
                    Moonlight.fontUtil.drawStringWithShadow(String.valueOf(ch), currentX, 2, Colors.INSTANCE.getColor(currentX, true));
                } else {
                    Moonlight.fontUtil.drawString(String.valueOf(ch), currentX, 2, Colors.INSTANCE.getColor(currentX, true));
                }
                currentX += Moonlight.fontUtil.getStringWidth(String.valueOf(ch));
            }
        } else {
            if (shadow.getValue()) {
                Moonlight.fontUtil.drawStringWithShadow(renderText, width / 2 - (int) Moonlight.fontUtil.getStringWidth(renderText) / 2 + 2, 2, Colors.INSTANCE.getColor(1, false));
            } else {
                Moonlight.fontUtil.drawString(renderText, width / 2 - (int) Moonlight.fontUtil.getStringWidth(renderText) / 2 + 2, 2, Colors.INSTANCE.getColor(1, false));
            }
        }
    }

    private void doCurrentConfig() {
        if(currentConfig.getValue()) {
            int offset = 1 + currentConfigOffset.getValue();
            if (ConfigCommand.INSTANCE == null || ConfigCommand.INSTANCE.lastLoadedConfig == null) {
                return;
            }
            if (currentConfigGradient.getValue()) {
                char[] characters = ("Config: " + ConfigCommand.INSTANCE.lastLoadedConfig).toCharArray();
                int currentX = 1;
                for (char ch : characters) {
                    if (shadow.getValue()) {
                        Moonlight.fontUtil.drawStringWithShadow(String.valueOf(ch), currentX, offset, Colors.INSTANCE.getColor(currentX, true));
                    } else {
                        Moonlight.fontUtil.drawString(String.valueOf(ch), currentX, offset, Colors.INSTANCE.getColor(currentX, true));
                    }
                    currentX += Moonlight.fontUtil.getStringWidth(String.valueOf(ch));
                }
            } else {
                if (shadow.getValue()) {
                    Moonlight.fontUtil.drawStringWithShadow(("Config: " + ConfigCommand.INSTANCE.lastLoadedConfig), 1, offset, Colors.INSTANCE.getColor(1, false));
                } else {
                    Moonlight.fontUtil.drawString(("Config: " + ConfigCommand.INSTANCE.lastLoadedConfig), 1, offset, Colors.INSTANCE.getColor(1, false));
                }
            }
        }
    }

    public static int getScale() {
        int scaleFactor = 0;
        int scale = mc.gameSettings.guiScale;
        if (scale == 0)
            scale = 1000;
        while (scaleFactor < scale &&mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240)
            scaleFactor++;
        if (scaleFactor == 0)
            scaleFactor = 1;
        return scaleFactor;
    }

    @SubscribeEvent
    public void onPotionRender(PotionHUDEvent event) {
        if(!potionIcons.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getType() == PacketEvent.PacketType.INCOMING) {
            serverLastUpdated = System.currentTimeMillis();
        }
    }

    private double timeDifference() {
        return MathUtil.round((System.currentTimeMillis() - serverLastUpdated) / 1000d, 1);
    }

    final Comparator<TextElement> sortByLength = (first, second) -> {
        final float dif = Moonlight.fontUtil.getStringWidth(second.getText()) - Moonlight.fontUtil.getStringWidth(first.getText());
        return dif != 0 ? (int) dif : second.getText().compareTo(first.getText());
    };

    @Override
    public void onToggle() {
        arrayListElements.clear();
        arrayTimer.reset();
    }

    private static class ArrayListElement {
        public Module module;
        public AnimationState state;
        public int ticks;

        public ArrayListElement(Module module) {
            this.module = module;
            this.ticks = 50;
            this.state = AnimationState.NONE;
        }
    }

    private static class TextElement {
        private final boolean isPotion;
        private final String text;
        private final int color;

        public TextElement(String text, int color, boolean ispot) {
            this.text = text;
            this.color = color;
            this.isPotion = ispot;
        }

        public String getText() {
            return this.text;
        }

        public int getColor() {
            return this.color;
        }

        public boolean isPot() {
            return this.isPotion;
        }
    }

    public enum CoordsSide {
        LEFT,
        RIGHT
    }

    public enum AnimationState {
        OPEN,
        CLOSE,
        NONE
    }

    private enum Page {
        GENERAL,
        WATERMARK
    }

    private enum Rendering {
        UP,
        DOWN
    }

    private enum WatermarkType {
        MOONLIGHT("Moonlight"),
        MOONLIGHT_IE("Moonlight.ie"),
        CUSTOM("Custom"),
        NONE("None");

        private final String name;

        WatermarkType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
