package me.fluffy.dactyl.config;

import net.minecraft.block.Block;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NukerManager {
    private final List<Block> nukerList;

    File dactylNuker;

    public NukerManager() {
        nukerList = new ArrayList<>();
        try {
            dactylNuker = ConfigUtil.createFileIfNotExists("nukerblocks", "yml");
            intializeBlocks();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean hasBlock(Block block) {
        return nukerList.contains(block);
    }

    public List<Block> getBlockList() {
        return this.nukerList;
    }

    public void intializeBlocks() throws IOException {
        InputStream friendStream = new FileInputStream(dactylNuker);
        Map<String, Map<String, Object>> yamlObj = new Yaml().load(friendStream);
        if(yamlObj != null) {
            for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                nukerList.add(Block.getBlockFromName(pathEntry.getKey()));
            }
        }
    }

    public void addBlock(String name) {
        if(Block.getBlockFromName(name) != null) {
            nukerList.add(Block.getBlockFromName(name));
        }
    }

    public void removeBlock(String name) {
        nukerList.removeIf(block->block != null && block.getLocalizedName() != null && name != null && block.getLocalizedName().replaceAll("\\s+","_").equalsIgnoreCase(name));
    }

    public void save() throws IOException {
        ConfigUtil.clearFile(dactylNuker);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        for (Block block : nukerList) {
            if(block.getLocalizedName() != null) {
                String name = block.getLocalizedName().replaceAll("\\s+","_");
                yamlData.put(name, "");
            }
        }
        Yaml yaml = new Yaml();
        BufferedWriter bufferedWriter = ConfigUtil.makeWriter(dactylNuker, false);
        yaml.dump(yamlData, bufferedWriter);
        ConfigUtil.closeWriter(bufferedWriter);
    }
}
