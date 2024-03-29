package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.command.Command;
import me.chloe.moonlight.module.impl.misc.Regear;
import me.chloe.moonlight.util.ChatUtil;
import net.minecraft.client.Minecraft;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class SelectKitCommand extends Command {
    public SelectKitCommand() {
        super("SelectKitCommand", new String[] {"selectkit"},"Select kits for Regear module.");
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
        if(!kitFile.exists()) {
            ChatUtil.printMsg("&cKit with name " + args[1] + " does not exist!", true, false);
            return;
        }
        ArrayList<CreateKitCommand.YMLLoadedItem> ymlItems = new ArrayList<>();
        try {
            InputStream fileInputStream = new FileInputStream(kitFile);
            Map<String, Map<String, Object>> yamlObj = new Yaml().load(fileInputStream);
            if (yamlObj != null) {
                for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                    ArrayList<CreateKitCommand.Enchantment> enchantments = new ArrayList<>();
                    String itemName = (String)pathEntry.getValue().get("ItemName");
                    Iterator it = pathEntry.getValue().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        if(!String.valueOf(pair.getKey()).equalsIgnoreCase("ItemName")) {
                            enchantments.add(new CreateKitCommand.Enchantment(Short.valueOf((String) pair.getKey()), Short.valueOf((String) pair.getValue())));
                        }
                        it.remove();
                    }
                    ymlItems.add(new CreateKitCommand.YMLLoadedItem(Integer.parseInt(pathEntry.getKey()), itemName, enchantments));
                }
            }
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            ChatUtil.printMsg("&cKit " + args[1] + " failed to load!", true, false);
            return;
        }
        ChatUtil.printMsg("&aKit " + args[1] + " loaded!", true, false);
        Regear.INSTANCE.ymlInventory = ymlItems;
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Moonlight.commandManager.getPrefix() +"selectkit <name>", true, false);
    }

    private boolean isIncorrect(String[] args) {
        if(args.length < 2) {
            return true;
        }
        return false;
    }
}
