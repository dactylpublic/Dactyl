package me.fluffy.dactyl.managers;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.ClickGuiModule;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.client.Components;
import me.fluffy.dactyl.module.impl.client.HUD;
import me.fluffy.dactyl.module.impl.combat.*;
import me.fluffy.dactyl.module.impl.misc.FakePlayer;
import me.fluffy.dactyl.module.impl.movement.*;
import me.fluffy.dactyl.module.impl.player.FastItem;
import me.fluffy.dactyl.module.impl.player.Freecam;
import me.fluffy.dactyl.module.impl.player.NoHitbox;
import me.fluffy.dactyl.module.impl.render.*;
import me.fluffy.dactyl.setting.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final ArrayList<Module> modules = new ArrayList<>();
    public ModuleManager() {


        // combat
        modules.add(new AutoCrystal());
        modules.add(new Offhand());
        modules.add(new Killaura());
        modules.add(new Criticals());
        modules.add(new AutoTrap());
        modules.add(new Surround());

        // player
        modules.add(new Freecam());
        modules.add(new FastItem());
        modules.add(new NoHitbox());

        // misc
        modules.add(new FakePlayer());

        // movement
        modules.add(new Strafe());
        modules.add(new NoSlow());
        modules.add(new Velocity());
        modules.add(new Step());
        modules.add(new ReverseStep());

        // render
        modules.add(new HoleESP());
        modules.add(new HandColor());
        modules.add(new Nametags());
        modules.add(new Fullbright());
        modules.add(new NoRender());

        // client
        modules.add(new ClickGuiModule());
        modules.add(new HUD());
        modules.add(new Colors());
        modules.add(new Components());

        setup();
    }

    public void setup() {
        sortModules();
        registerSettings();
    }

    public void sortModules() {
        Comparator<Module> alphaComp = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());
        modules.sort(alphaComp);
    }

    public ArrayList<Module> getModules() {
        return this.modules;
    }

    public List<Module> getEnabledModules() {
        return this.modules.stream().filter(mod -> !mod.isEnabled()).collect(Collectors.toList());
    }

    public void onRender(Render3DEvent event) {
        getModules().stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(event));
    }


    public void registerSettings() {
        for(Module module : modules) {
            try {
                for (Field field : module.getClass().getDeclaredFields()) {
                    if (Setting.class.isAssignableFrom(field.getType())) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Setting setting = (Setting) field.get(module);
                        module.register(setting);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
