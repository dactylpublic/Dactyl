package me.fluffy.dactyl.command.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.command.Command;
import me.fluffy.dactyl.config.ConfigUtil;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.Minecraft;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateKitCommand extends Command {
    public CreateKitCommand() {
        super("CreateKitCommand", new String[] {"createkit"},"Create kits for Regear module.");
    }

    @Override
    public void run(String[] args) {
        if(isIncorrect(args)) {
            handleIncorrectUsage();
            return;
        }
        handleCreateKit(args);
    }

    private void handleCreateKit(String[] args) {
        if(Minecraft.getMinecraft().player == null) {
            return;
        }
        File kitFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"kits"+File.separator+args[1]+".yml"+File.separator);
        BufferedWriter bufferedWriter = null;
        try {
            ConfigUtil.createFileIfNotExistsRaw(kitFile);
            bufferedWriter = ConfigUtil.makeWriter(kitFile, true);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if(bufferedWriter != null) {
            Map<String, Object> yamlData = new HashMap<String, Object>();
            /*yamlData.put("Enabled", module.isEnabled());
            for (Setting setting : module.getSettingsList()) {
                if (setting.getValue() instanceof Bind) {
                    Map<String, Object> nestedKey = new HashMap<String, Object>();
                    nestedKey.put("value", ((Bind) setting.getValue()).getKey());
                    yamlData.put(setting.getName(), nestedKey);
                } else if (setting.getValue() instanceof Enum) {
                    yamlData.put(setting.getName(), getEnumName((Enum) setting.getValue()));
                } else {
                    yamlData.put(setting.getName(), setting.getValue());
                }
            }
            Yaml yaml = new Yaml();
            yaml.dump(yamlData, bufferedWriter);
            try {
                ConfigUtil.closeWriter(bufferedWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }*/
        }
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Dactyl.commandManager.getPrefix() +"createkit <name>", true, false);
    }

    private boolean isIncorrect(String[] args) {
        if(args.length < 2) {
            return true;
        }
        return false;
    }
}
