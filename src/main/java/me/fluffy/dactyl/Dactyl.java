package me.fluffy.dactyl;

import me.fluffy.dactyl.config.Configuration;
import me.fluffy.dactyl.config.friends.FriendManager;
import me.fluffy.dactyl.gui.ClickGUI;
import me.fluffy.dactyl.managers.ListenerManager;
import me.fluffy.dactyl.managers.ModuleManager;
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
    public static final String VERSION = "0.0.1a";

    public static Logger logger;

    public static final Dactyl INSTANCE = new Dactyl();

    public static ModuleManager moduleManager;
    public static FontUtil fontUtil;
    public static ListenerManager listenerManager;
    public static ClickGUI clickGUI;
    public static Configuration configuration;
    public static FriendManager friendManager;

    @Mod.EventHandler
    public void preInitialization(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void initialization(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        fontUtil = new FontUtil();
        listenerManager = new ListenerManager();
        clickGUI = new ClickGUI();
        configuration = new Configuration();
        friendManager = new FriendManager();
    }

    @Mod.EventHandler
    public void postInitialization(FMLPostInitializationEvent event) {
        Display.setTitle("ICEHack " + VERSION);
    }

    public void save() {
        try {
            friendManager.save();
            Configuration.save("", true);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

}
