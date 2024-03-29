package me.chloe.moonlight.managers;

import me.chloe.moonlight.module.impl.client.*;
import me.chloe.moonlight.module.impl.combat.*;
import me.chloe.moonlight.module.impl.misc.*;
import me.chloe.moonlight.module.impl.movement.*;
import me.chloe.moonlight.module.impl.player.*;
import me.chloe.moonlight.module.impl.render.*;
import me.chloe.moonlight.event.impl.world.Render3DEvent;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.module.impl.misc.Blink;
import me.chloe.moonlight.setting.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final ArrayList<Module> modules = new ArrayList<>();
    public ModuleManager() {
        // combat
        modules.add(new Offhand());
        modules.add(new Killaura());
        modules.add(new Criticals());
        modules.add(new AutoTrap());
        modules.add(new SelfTrap());
        modules.add(new AutoArmor());
        modules.add(new HoleFill());
        modules.add(new CrystalBomber());
        modules.add(new AutoCrystal());
        modules.add(new AutoHoleMine());
        modules.add(new Surround());
        //modules.add(new Auto32k());

        // player
        modules.add(new Freecam());
        modules.add(new FastItem());
        modules.add(new NoHitbox());
        modules.add(new Scaffold());
        modules.add(new XCarry());
        modules.add(new JumpFill());
        modules.add(new AntiInteract());
        modules.add(new Vanish());
        modules.add(new Blink());
        modules.add(new AntiHunger());
        modules.add(new MultiTask());
        modules.add(new HotbarRefill());
        modules.add(new PingSpoof());
        modules.add(new PacketFly());
        modules.add(new AutoDupe());
        modules.add(new BypassCrystal());
        modules.add(new Nuker());

        // misc
        modules.add(new FakePlayer());
        modules.add(new MCF());
        modules.add(new PortalGodMode());
        modules.add(new AutoReconnect());
        modules.add(new MiningTweaks());
        modules.add(new AutoRespawn());
        modules.add(new Chat());
        modules.add(new TimerModule());
        modules.add(new Notifications());
        modules.add(new PacketLogger());
        modules.add(new Spammer());
        modules.add(new ExtraTab());
        modules.add(new Locator());
        modules.add(new ChatTimestamps());
        modules.add(new Regear());
        modules.add(new HClip());
        modules.add(new VClip());

        // movement
        modules.add(new Strafe());
        modules.add(new NoSlow());
        modules.add(new Velocity());
        modules.add(new Step());
        modules.add(new ReverseStep());
        modules.add(new LongJump());
        modules.add(new Sprint());
        modules.add(new BoatFly());
        modules.add(new Jesus());
        modules.add(new AntiVoid());
        modules.add(new AltSpeed());
        modules.add(new EntitySpeed());
        modules.add(new EntityControl());
        modules.add(new IceSpeed());
        modules.add(new ElytraFly());
        modules.add(new AntiLevitation());
        modules.add(new AutoPearl());
        modules.add(new Flight());
        modules.add(new NoFall());

        // render
        modules.add(new HoleESP());
        modules.add(new HandColor());
        modules.add(new Nametags());
        modules.add(new Fullbright());
        modules.add(new NoRender());
        modules.add(new ViewModel());
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new StorageESP());
        modules.add(new LogoutSpots());
        modules.add(new XRay());
        modules.add(new VoidESP());
        modules.add(new ViewClip());
        modules.add(new Chams());
        modules.add(new Trajectories());
        modules.add(new BlockHighlight());
        modules.add(new NewChunks());
        modules.add(new EnchantColor());
        modules.add(new PearlTrails());

        // client
        modules.add(new ClickGuiModule());
        modules.add(new HUD());
        modules.add(new Colors());
        modules.add(new Components());
        modules.add(new Media());
        modules.add(new Crasher());

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

    public void onLogout() {
        getModules().stream().filter(Module::isEnabled).forEach(module -> module.onLogout());
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
