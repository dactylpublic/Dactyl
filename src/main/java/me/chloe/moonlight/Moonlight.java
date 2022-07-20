package me.chloe.moonlight;

import me.chloe.moonlight.config.NukerManager;
import me.chloe.moonlight.gui.ClickGUI;
import me.chloe.moonlight.config.Configuration;
import me.chloe.moonlight.config.XRayManager;
import me.chloe.moonlight.config.friends.FriendManager;
import me.chloe.moonlight.managers.*;
import me.chloe.moonlight.util.SoundUtil;
import me.chloe.moonlight.util.render.font.FontUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = Moonlight.MODID, name = Moonlight.NAME, version = Moonlight.VERSION)
public class Moonlight {
    public static final String MODID = "moonlight";
    public static final String NAME = "Moonlight";
    public static final String VERSION = "v1.7.4";

    public static Logger logger;

    public static final Moonlight INSTANCE = new Moonlight();

    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static FontUtil fontUtil;
    public static ListenerManager listenerManager;
    public static ClickGUI clickGUI;
    public static Configuration configuration;
    public static FriendManager friendManager;
    public static TickRateManager tickRateManager;
    public static TotemManager totemManager;
    public static XRayManager xRayManager;
    public static NukerManager nukerManager;

    @Mod.EventHandler
    public void preInitialization(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        SoundUtil.playStartupSound();
    }

    @Mod.EventHandler
    public void initialization(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        fontUtil = new FontUtil();
        listenerManager = new ListenerManager();
        clickGUI = new ClickGUI();
        configuration = new Configuration();
        friendManager = new FriendManager();
        tickRateManager = new TickRateManager();
        totemManager = new TotemManager();
        xRayManager = new XRayManager();
        nukerManager = new NukerManager();
    }

    @Mod.EventHandler
    public void postInitialization(FMLPostInitializationEvent event) {
        Display.setTitle("Moonlight " + VERSION);
    }

    public void save() {
        try {
            friendManager.save();
            xRayManager.save();
            nukerManager.save();
            Configuration.save("", true);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public TickRateManager getTickRateManager() {
        if (this.tickRateManager == null) {
            this.tickRateManager = new TickRateManager();
        }
        return this.tickRateManager;
    }

}
