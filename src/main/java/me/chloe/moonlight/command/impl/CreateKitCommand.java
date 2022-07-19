package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.command.Command;
import me.chloe.moonlight.config.ConfigUtil;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.util.CombatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        ArrayList<YMLItem> ymlItems = new ArrayList<>();
        for (int x = 0; x < Minecraft.getMinecraft().player.inventoryContainer.getInventory().size(); x++) {
            if(CombatUtil.xcarryAndArmor.contains(x)) {
                continue;
            }
            ItemStack stack = Minecraft.getMinecraft().player.inventoryContainer.getInventory().get(x);
            if(stack.isEmpty()) {
                continue;
            }
            ymlItems.add(new YMLItem(x, stack));
        }
        File kitFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"kits"+File.separator+args[1]+".yml"+File.separator);
        BufferedWriter bufferedWriter = null;
        try {
            ConfigUtil.createFileIfNotExistsRaw(kitFile);
            ConfigUtil.clearFile(kitFile);
            bufferedWriter = ConfigUtil.makeWriter(kitFile, true);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if(bufferedWriter != null) {
            for(YMLItem ymlItem : ymlItems) {
                Map<String, Object> yamlData = new HashMap<String, Object>();
                Map<String, Object> itemInfo = new HashMap<String, Object>();

                itemInfo.put("ItemName", ymlItem.getItemStack().getItem().getItemStackDisplayName(ymlItem.getItemStack()));
                NBTTagList enchants = ymlItem.getEnchantmentTagList();
                if (enchants != null) {
                    for (int index = 0; index < enchants.tagCount(); ++index) {
                        short id = enchants.getCompoundTagAt(index).getShort("id");
                        short level = enchants.getCompoundTagAt(index).getShort("lvl");
                        itemInfo.put(String.valueOf(id), String.valueOf(level));
                    }
                }
                yamlData.put(String.valueOf(ymlItem.getSlot()), itemInfo);

                Yaml yaml = new Yaml();
                yaml.dump(yamlData, bufferedWriter);
            }
            try {
                ConfigUtil.closeWriter(bufferedWriter);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            ChatUtil.printMsg("&cError creating kit", true, false);
            return;
        }
        ChatUtil.printMsg("&aKit "+args[1]+" created successfully!", true, false);
    }

    public static class Enchantment {
        public short id;
        public short level;
        public Enchantment(short id, short level) {
            this.id = id;
            this.level = level;
        }
    }

    public static class YMLLoadedItem {
        private int slot;
        private String itemName;
        private ArrayList<Enchantment> enchantments;
        public YMLLoadedItem(int slot, String itemName, ArrayList<Enchantment> enchantments) {
            this.slot = slot;
            this.itemName = itemName;
            this.enchantments = enchantments;
        }

        public String getItemName() {
            return this.itemName;
        }

        public int getSlot() {
            return this.slot;
        }

        public ArrayList<Enchantment> getEnchantments() {
            return this.enchantments;
        }
    }

    public static class YMLItem {
        private int slot;
        private ItemStack itemStack;
        private NBTTagList enchantments = null;
        public YMLItem(int slot, ItemStack itemStack) {
            this.slot = slot;
            this.itemStack = itemStack;
            this.enchantments = itemStack.getEnchantmentTagList();
        }

        public int getSlot() {
            return this.slot;
        }

        public NBTTagList getEnchantmentTagList() {
            return this.enchantments;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Moonlight.commandManager.getPrefix() +"createkit <name>", true, false);
    }

    private boolean isIncorrect(String[] args) {
        if(args.length < 2) {
            return true;
        }
        return false;
    }
}
