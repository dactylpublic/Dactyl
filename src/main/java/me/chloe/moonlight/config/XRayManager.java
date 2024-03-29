package me.chloe.moonlight.config;

import net.minecraft.block.Block;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XRayManager {
    private final List<Block> xrayList;

    File MoonlightXRay;

    public XRayManager() {
        xrayList = new ArrayList<>();
        try {
            MoonlightXRay = ConfigUtil.createFileIfNotExists("xrayblocks", "yml");
            intializeBlocks();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean hasBlock(Block block) {
        return xrayList.contains(block);
    }

    public List<Block> getBlockList() {
        return this.xrayList;
    }

    public void intializeBlocks() throws IOException {
        InputStream friendStream = new FileInputStream(MoonlightXRay);
        Map<String, Map<String, Object>> yamlObj = new Yaml().load(friendStream);
        if(yamlObj != null) {
            for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                xrayList.add(Block.getBlockFromName(pathEntry.getKey()));
            }
        }
    }

    public void addBlock(String name) {
        if(Block.getBlockFromName(name) != null) {
            xrayList.add(Block.getBlockFromName(name));
        }
    }

    public void removeBlock(String name) {
        xrayList.removeIf(block->block != null && block.getLocalizedName() != null && name != null && block.getLocalizedName().replaceAll("\\s+","_").equalsIgnoreCase(name));
    }

    public void save() throws IOException {
        ConfigUtil.clearFile(MoonlightXRay);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        for (Block block : xrayList) {
            if(block.getLocalizedName() != null) {
                String name = block.getLocalizedName().replaceAll("\\s+","_");
                yamlData.put(name, "");
            }
        }
        Yaml yaml = new Yaml();
        BufferedWriter bufferedWriter = ConfigUtil.makeWriter(MoonlightXRay, false);
        yaml.dump(yamlData, bufferedWriter);
        ConfigUtil.closeWriter(bufferedWriter);
    }
}
