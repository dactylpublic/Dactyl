package me.fluffy.dactyl;

import me.fluffy.dactyl.config.Configuration;
import me.fluffy.dactyl.config.friends.FriendManager;
import me.fluffy.dactyl.gui.ClickGUI;
import me.fluffy.dactyl.managers.*;
import me.fluffy.dactyl.util.render.font.FontUtil;
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
    public static final String VERSION = "v0.8.4";

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

    @Mod.EventHandler
    public void preInitialization(FMLPreInitializationEvent event) {
        logger = event.getModLog();
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
    }

    @Mod.EventHandler
    public void postInitialization(FMLPostInitializationEvent event) {
        Display.setTitle("Dactyl.ie " + VERSION);
    }

    public void save() {
        try {
            friendManager.save();
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
