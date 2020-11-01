package me.fluffy.dactyl.module.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.render.PotionHUDEvent;
import me.fluffy.dactyl.injection.inj.access.IMinecraft;
import me.fluffy.dactyl.injection.inj.access.ITimer;
import me.fluffy.dactyl.listener.impl.SpeedListener;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.MathUtil;
import me.fluffy.dactyl.util.TimeUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

public class HUD extends Module {

    public Setting<Boolean> customFont = new Setting<Boolean>("CustomFont", true);
    public Setting<Boolean> renderHud = new Setting<Boolean>("RenderHUD", true);
    public Setting<Boolean> potionEffects = new Setting<Boolean>("Potions", true, v->renderHud.getValue());
    public Setting<Boolean> potionColorSync = new Setting<Boolean>("PotionCSync", false, v->renderHud.getValue());
    public Setting<Boolean> potionIcons = new Setting<Boolean>("PotionIcons", false);
    public Setting<Boolean> arrayListSetting = new Setting<Boolean>("ArrayList", true, v->renderHud.getValue());
    public Setting<Integer> arrayListUpdates = new Setting<Integer>("ArrayUpdates", 200, 50, 1000, v->renderHud.getValue()&&arrayListSetting.getValue());
    public Setting<Integer> arrayListFactor = new Setting<Integer>("AnimFactor", 2, 1, 10, v->renderHud.getValue()&&arrayListSetting.getValue());
    public Setting<Boolean> fps = new Setting<Boolean>("FPS", true, v->renderHud.getValue());
    public Setting<Boolean> tps = new Setting<Boolean>("TPS", true, v->renderHud.getValue());
    public Setting<Boolean> speed = new Setting<Boolean>("Speed", true, v->renderHud.getValue());
    public Setting<Boolean> ping = new Setting<Boolean>("Ping", true, v->renderHud.getValue());
    public Setting<Boolean> clock = new Setting<Boolean>("Clock", true, v->renderHud.getValue());
    public Setting<Boolean> lagNotifier = new Setting<Boolean>("LagNotifier", true, v->renderHud.getValue());
    public Setting<Rendering> renderingSetting = new Setting<Rendering>("Rendering", Rendering.UP, v->renderHud.getValue());

    public Setting<WatermarkType> watermarkTypeSetting = new Setting<WatermarkType>("Logo", WatermarkType.DACTYL_IE, v->renderHud.getValue());
    public Setting<String> customWatermark = new Setting<String>("CustomWatermark", "Trollgod.cc", vis->renderHud.getValue() && watermarkTypeSetting.getValue() == WatermarkType.CUSTOM);
    public Setting<Boolean> gradientLogo = new Setting<Boolean>("LogoGradient", false, v->renderHud.getValue());

    public Setting<Boolean> shadow = new Setting<Boolean>("Shadow", false, v->renderHud.getValue());



    public static HUD INSTANCE;
    public HUD() {
        super("HUD", Category.CLIENT, true);
        INSTANCE = this;
    }

    public static final ArrayList<ArrayListElement> arrayListElements = new ArrayList<>();
    public static final TimeUtil arrayTimer = new TimeUtil();

    private long serverLastUpdated;

    @Override
    public void onClientUpdate() {
        if(mc.player == null) {
            return;
        }
        if(arrayTimer.hasPassed(arrayListUpdates.getValue())) {
            updateArrayList();
            sortModules();
        }
    }

    private void updateArrayList() {
        for(Module module : Dactyl.moduleManager.getModules()) {
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
            String firstName = first.module.getDisplayName() + (first.module.getModuleInfo().length() > 0 ? first.module.getModuleInfo()+"[]" : "");
            String secondName = second.module.getDisplayName() + (second.module.getModuleInfo().length() > 0 ? second.module.getModuleInfo()+"[]" : "");
            float dif = Dactyl.fontUtil.getStringWidth(secondName) -  Dactyl.fontUtil.getStringWidth(firstName);
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
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
            doOtherRender();
        }
    }

    private void doArrayList() {
        if(arrayListSetting.getValue()) {
            if(renderingSetting.getValue() == Rendering.UP) {
                int textY = 1;
                for(ArrayListElement element : arrayListElements) {
                    String moduleInfoString = TextFormatting.GRAY + " [" + TextFormatting.WHITE + element.module.getModuleInfo() + TextFormatting.GRAY + "]";
                    String renderString = element.module.getDisplayName() + (element.module.hasModuleInfo() ? moduleInfoString : "");
                    int currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2;

                    if(element.state == AnimationState.OPEN) {
                        element.ticks = element.ticks-arrayListFactor.getValue();
                        if(element.ticks <= 0) {
                            element.state = AnimationState.NONE;
                        } else {
                            currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2 + element.ticks;
                        }
                    } else if(element.state == AnimationState.CLOSE) {
                        if(element.ticks <= 0) {
                            element.ticks = 1;
                        }
                        element.ticks+=arrayListFactor.getValue();
                        currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2 + element.ticks;
                    }
                    if (shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Dactyl.fontUtil.drawString(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                    textY+=10;
                }
                arrayListElements.removeIf(element-> element.state == AnimationState.CLOSE && element.ticks >= 50);
            } else {
                int textY = RenderUtil.getScreenHeight()-(arrayListElements.size()*10)-1;
                if(mc.currentScreen instanceof GuiChat) textY = textY-15;
                for (int i = arrayListElements.size() - 1; i >= 0; i--) {
                    if(arrayListElements.get(i) == null) {
                        return;
                    }
                    String moduleInfoString = TextFormatting.GRAY + " [" + TextFormatting.WHITE + arrayListElements.get(i).module.getModuleInfo() + TextFormatting.GRAY + "]";
                    String renderString = arrayListElements.get(i).module.getDisplayName() + (arrayListElements.get(i).module.hasModuleInfo() ? moduleInfoString : "");
                    int currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2;

                    if(arrayListElements.get(i).state == AnimationState.OPEN) {
                        arrayListElements.get(i).ticks = arrayListElements.get(i).ticks-arrayListFactor.getValue();
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).state = AnimationState.NONE;
                        } else {
                            currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                        }
                    } else if(arrayListElements.get(i).state == AnimationState.CLOSE) {
                        if(arrayListElements.get(i).ticks <= 0) {
                            arrayListElements.get(i).ticks = 1;
                        }
                        arrayListElements.get(i).ticks+=arrayListFactor.getValue();
                        currentX = (int) (RenderUtil.getScreenWidth() - Dactyl.fontUtil.getStringWidth(renderString)) - 2 + arrayListElements.get(i).ticks;
                    }
                    if (shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Dactyl.fontUtil.drawString(renderString, currentX, textY, Colors.INSTANCE.getColor(textY, false));
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
            if (effects != null && !effects.isEmpty()) {
                for (PotionEffect effect : effects) {
                    if (effect != null) {
                        Potion potion = effect.getPotion();
                        if (potion != null) {
                            String name = I18n.format(potion.getName());
                            if (effect.getAmplifier() == 1)
                            {
                                name+=" 2";
                            }
                            else if (effect.getAmplifier() == 2)
                            {
                                name+=" 3";
                            }
                            else if (effect.getAmplifier() == 3)
                            {
                                name+=" 4";
                            }
                            name += TextFormatting.WHITE + " "+Potion.getPotionDurationString(effect, 1.0F);
                            pots.add(new TextElement(name, potion.getLiquidColor(), !potionColorSync.getValue()));
                            pots.sort(sortByLength);
                        }
                    }
                }
            }
        }

        if(fps.getValue()) {
            normal.add(new TextElement(TextFormatting.RESET + "FPS " + TextFormatting.WHITE + String.valueOf(mc.getDebugFPS()), 0xffffffff, false));
        }

        if(tps.getValue()) {
            float tick = Dactyl.INSTANCE.getTickRateManager().getTickRate();
            float rounded = (float) ((float)Math.round(tick * 100.0) / 100.0);
            normal.add(new TextElement(TextFormatting.RESET + "TPS " + TextFormatting.WHITE + String.valueOf(rounded), 0xffffffff, false));
        }

        if(ping.getValue()) {
            int ms = EntityUtil.getPing(mc.player);
            String msText = ChatFormatting.RESET + "Ping " + ChatFormatting.WHITE + String.valueOf(ms) + "ms";
            normal.add(new TextElement(msText, 0xffffffff, false));
        }

        if(clock.getValue()) {
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            int mins = rightNow.get(Calendar.MINUTE);
            boolean isAfternoon = false;
            if(hour > 12) isAfternoon = true;
            String hourString = isAfternoon ? String.valueOf(hour-12) : String.valueOf(hour);
            String textTime = ChatFormatting.RESET + "Time " + ChatFormatting.WHITE + hourString +":"+(mins < 10 ? "0" : "")+String.valueOf(mins) + (isAfternoon ? "pm" : "am");
            normal.add(new TextElement(textTime, 0xffffffff, false));
        }

        if(speed.getValue()) {
            double accurateSpeed = (50f / ((ITimer)((IMinecraft)mc).getTimer()).getTickLength())*SpeedListener.INSTANCE.getSpeedKpH();
            normal.add(new TextElement(TextFormatting.RESET + "Speed " + TextFormatting.WHITE + String.format( "%.2f", accurateSpeed) + " km/h", 0xffffffff, false));
        }


        if(lagNotifier.getValue()) {
            String lag = TextFormatting.GRAY + "Server is not responding " + String.valueOf(timeDifference()) + "s";
            if ((1000L <= System.currentTimeMillis() - serverLastUpdated) && !(mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat))) {
                int divider = getScale();
                int lagX = (int) (mc.displayWidth / divider / 2 - Dactyl.fontUtil.getStringWidth(lag) / 2);
                int lagY = 10;
                if(shadow.getValue()) {
                    Dactyl.fontUtil.drawStringWithShadow(lag, lagX, lagY, 0xffffffff);
                } else {
                    Dactyl.fontUtil.drawString(lag, lagX, lagY, 0xffffffff);
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
                int thisX = (int) (RenderUtil.getScreenWidth()-(Dactyl.fontUtil.getStringWidth(text.get(i).getText()))-2);
                if(text.get(i).isPot()) {
                    if(shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(text.get(i).getText(), thisX, textY, text.get(i).getColor());
                    } else {
                        Dactyl.fontUtil.drawString(text.get(i).getText(), thisX, textY, text.get(i).getColor());
                    }
                } else {
                    if(shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(text.get(i).getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Dactyl.fontUtil.drawString(text.get(i).getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                }
                textY+=10;
            }
        } else {
            int textY = 1;
            for (TextElement s : text) {
                int thisX = (int) (RenderUtil.getScreenWidth()-(Dactyl.fontUtil.getStringWidth(s.getText()))-2);
                if(s.isPot()) {
                    if(shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(s.getText(), thisX, textY, s.getColor());
                    } else {
                        Dactyl.fontUtil.drawString(s.getText(), thisX, textY, s.getColor());
                    }
                } else {
                    if(shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(s.getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    } else {
                        Dactyl.fontUtil.drawString(s.getText(), thisX, textY, Colors.INSTANCE.getColor(textY, false));
                    }
                }
                textY+=10;
            }
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

    private void doWatermark() {
        if(watermarkTypeSetting.getValue() != WatermarkType.NONE) {
            String drawingWatermark = "";
            switch((WatermarkType)watermarkTypeSetting.getValue()) {
                case DACTYL_IE:
                    drawingWatermark = "Dactyl.ie " + Dactyl.VERSION;
                    break;
                case DACTYL:
                    drawingWatermark = "Dactyl " + Dactyl.VERSION;
                    break;
                case CUSTOM:
                    drawingWatermark = customWatermark.getValue().replace("<v>", Dactyl.VERSION);
                    break;
            }
            if(gradientLogo.getValue()) {
                char[] characters = drawingWatermark.toCharArray();
                int currentX = 1;
                for(char ch : characters) {
                    if(shadow.getValue()) {
                        Dactyl.fontUtil.drawStringWithShadow(String.valueOf(ch), currentX, 1, Colors.INSTANCE.getColor(currentX, true));
                    } else {
                        Dactyl.fontUtil.drawString(String.valueOf(ch), currentX, 1, Colors.INSTANCE.getColor(currentX, true));
                    }
                    currentX+=Dactyl.fontUtil.getStringWidth(String.valueOf(ch));
                }
            } else {
                if(shadow.getValue()) {
                    Dactyl.fontUtil.drawStringWithShadow(drawingWatermark, 1, 1, Colors.INSTANCE.getColor(1, false));
                } else {
                    Dactyl.fontUtil.drawString(drawingWatermark, 1, 1, Colors.INSTANCE.getColor(1, false));
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
        serverLastUpdated = System.currentTimeMillis();
    }

    private double timeDifference() {
        return MathUtil.round((System.currentTimeMillis() - serverLastUpdated) / 1000d, 1);
    }

    final Comparator<TextElement> sortByLength = (first, second) -> {
        final float dif = Dactyl.fontUtil.getStringWidth(second.getText()) -  Dactyl.fontUtil.getStringWidth(first.getText());
        return dif != 0 ? (int) dif : second.getText().compareTo(first.getText());
    };

    @Override
    public void onToggle() {
        arrayListElements.clear();
        arrayTimer.reset();
    }

    private class ArrayListElement {
        public Module module;
        public AnimationState state;
        public int ticks;

        public ArrayListElement(Module module) {
            this.module = module;
            this.ticks = 50;
            this.state = AnimationState.NONE;
        }
    }

    private class TextElement {
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
        DACTYL("Dactyl"),
        DACTYL_IE("Dactyl.ie"),
        CUSTOM("Custom"),
        NONE("None");

        private final String name;

        private WatermarkType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
