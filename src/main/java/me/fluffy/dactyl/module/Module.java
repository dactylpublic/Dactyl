package me.fluffy.dactyl.module;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private String name, desc, moduleInfo, displayName;
    private boolean enabled, seen, alwaysListening;
    private Category category;

    private final List<Setting> settingList = new ArrayList<>();

    public static Minecraft mc = Minecraft.getMinecraft();

    public Module(String name, Category category, String desc) {
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.moduleInfo = "";
        this.displayName = name;
        this.seen = true;
        this.alwaysListening = false;
        setupSettings();
    }

    public Module(String name, Category category) {
        this.name = name;
        this.desc = "";
        this.category = category;
        this.moduleInfo = "";
        this.displayName = name;
        this.seen = true;
        this.alwaysListening = false;
        setupSettings();
    }

    public Module(String name, Category category, String desc, boolean alwaysListening) {
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.moduleInfo = "";
        this.displayName = name;
        this.seen = true;
        this.alwaysListening = alwaysListening;
        setupSettings();
    }

    public Module(String name, Category category, boolean alwaysListening) {
        this.name = name;
        this.desc = "";
        this.category = category;
        this.moduleInfo = "";
        this.displayName = name;
        this.seen = true;
        this.alwaysListening = alwaysListening;
        setupSettings();
    }

    private void setupSettings() {
        this.register(new Setting<Bind>("Bind", new Bind(Keyboard.KEY_NONE)));
        this.register(new Setting<Boolean>("Hidden", false));
    }

    public void toggleWithReason(String reason) {
        ChatUtil.printMsg(reason, true, false);
        this.toggle();
    }

    public void toggle() {
        this.enabled = !enabled;
        if(enabled) {
            MinecraftForge.EVENT_BUS.register(this);
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        if(this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
        onToggle();
    }

    public void onLogout() {}
    public void onToggle() {}
    public void onEnable() {}
    public void onDisable() {}
    public void onClientUpdate() {}
    public void onScreen() {}
    public void onScreen2D(float partialTicks) {}
    public void onRender3D(Render3DEvent event) {}

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getModuleInfo() {
        return this.moduleInfo;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isHidden() {
        return ((Setting<Boolean>)this.getSetting("Hidden")).getValue();
    }

    public boolean hasModuleInfo() { return (this.moduleInfo.length() > 0); }

    public boolean isSeen() {
        return this.seen;
    }

    public void setModuleInfo(String moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public boolean isAlwaysListening() {
        return this.alwaysListening;
    }

    public void setAlwaysListening(boolean alwaysListening) {
        this.alwaysListening = alwaysListening;
    }

    public int getKey() {
        return ((Setting<Bind>)this.getSettingsList().get(0)).getValue().getKey();
    }

    public Category getCategory() {
        return this.category;
    }

    public void setKey(int key) {
        ((Setting<Bind>)this.getSettingsList().get(0)).getValue().setKey(key);
    }

    public void register(Setting setting) {
        this.settingList.add(setting);
    }

    public List<Setting> getSettingsList() {
        return this.settingList;
    }

    public ScaledResolution getRes() {
        return new ScaledResolution(mc);
    }

    public List<String> getSettingNames() {
        List<String> settingNames = new ArrayList<>();
        for(Setting setting : getSettingsList()) {
            settingNames.add(setting.getName());
        }
        return settingNames;
    }

    public Setting getSetting(String name) {
        for(Setting setting : getSettingsList()) {
            if(setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public enum Category {
        COMBAT,
        PLAYER,
        MISC,
        MOVEMENT,
        RENDER,
        CLIENT;

        @Override
        public String toString() {
            return name().toLowerCase().substring(0, 1).toUpperCase() + name().toLowerCase().substring(1);
        }
    }
}
