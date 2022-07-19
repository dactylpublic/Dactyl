package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.config.Configuration;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.command.Command;

public class ConfigCommand extends Command {
    public static ConfigCommand INSTANCE;
    public ConfigCommand() {
        super("Config", new String[] {"config", "conf"},"Manage the current config.");
        INSTANCE = this;
    }
    public String lastLoadedConfig = "None";

    @Override
    public void run(String[] args) {
        if(args.length != 3) {
            handleIncorrectUsage();
            return;
        }
        switch(args[1].toLowerCase()) {
            case "save":
                handleSaveConfig(args);
                break;
            case "load":
                handleLoadConfig(args);
                break;
            default:
                handleIncorrectUsage();
                break;
        }
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Moonlight.commandManager.getPrefix() +"config <load/save> <config>", true, false);
    }

    private void handleSaveConfig(String[] args) {
        try {
            Configuration.save(args[2], false);
        } catch(Exception exception) {
            exception.printStackTrace();
            ChatUtil.printMsg("&cConfiguration " + args[2] + " failed to save...", true, true);
            return;
        }
        ChatUtil.printMsg("&aConfiguration " + args[2] + " saved!", true, true);
    }

    private void handleLoadConfig(String[] args) {
        boolean loader = false;
        try {
            loader = Configuration.load(args[2], false);
        } catch(Exception exception) {
            //exception.printStackTrace();
            ChatUtil.printMsg("&cConfiguration " + args[2] + " does not exist.", true, true);
            return;
        }
        if(!loader) {
            ChatUtil.printMsg("&cConfiguration " + args[2] + " does not exist.", true, true);
            return;
        }
        lastLoadedConfig = args[2];
        ChatUtil.printMsg("&aConfiguration " + args[2] + " loaded!", true, true);
    }
}
