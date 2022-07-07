package me.chloe.dactyl;

import me.chloe.dactyl.config.NukerManager;
import me.chloe.dactyl.gui.ClickGUI;
import me.chloe.dactyl.config.Configuration;
import me.chloe.dactyl.config.XRayManager;
import me.chloe.dactyl.config.friends.FriendManager;
import me.chloe.dactyl.managers.*;
import me.chloe.dactyl.managers.*;
import me.chloe.dactyl.util.SoundUtil;
import me.chloe.dactyl.util.render.font.FontUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = Dactyl.MODID, name = Dactyl.NAME, version = Dactyl.VERSION)
public class Dactyl {
    public static final String MODID = "dactyl";
    public static final String NAME = "Dactyl";
    public static final String VERSION = "v1.7.1";

    public static Logger logger;

    public static final Dactyl INSTANCE = new Dactyl();

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
        Display.setTitle("Dactyl.ie " + VERSION);
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
