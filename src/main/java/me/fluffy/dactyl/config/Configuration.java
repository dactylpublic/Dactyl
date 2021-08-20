package me.fluffy.dactyl.config;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import net.minecraft.client.Minecraft;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Configuration {


    public Configuration() {
        try {
            createIfNotExists("", true);
            load("", true);
        } catch(IOException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean load(String configName, boolean isMainConfig) throws IOException, IllegalAccessException {
        File configFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"config" + sep + configName +File.separator);
        if(!configFile.exists() && !isMainConfig) {
            return false;
        }
        for(Module mod : Dactyl.moduleManager.getModules()) {
            try {
                File loadingModuleDir = (isMainConfig) ? ConfigUtil.makeClientDirFile("config" + sep + "Loaded Config" + sep + mod.getCategory().toString() + sep + mod.getName() + ".yml") : ConfigUtil.makeClientDirFile("config" + sep + configName + sep + mod.getCategory().toString() + sep + mod.getName() + ".yml");
                InputStream fileInputStream = new FileInputStream(loadingModuleDir);
                Map<String, Map<String, Object>> yamlObj = new Yaml().load(fileInputStream);
                if (yamlObj != null) {
                    for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                        if (mod.getSetting(pathEntry.getKey()) != null) {
                            Setting setting = mod.getSetting(pathEntry.getKey());
                            if (setting.getValue() instanceof Bind) {
                                Bind ymlBind = new Bind((int) pathEntry.getValue().get("value"));
                                mod.getSetting(pathEntry.getKey()).setValue(ymlBind);
                            } else if (setting.getValue() instanceof Enum) {
                                Enum enumClass = (Enum) setting.getValue();
                                for (Field field : enumClass.getClass().getDeclaredFields()) {
                                    if (Enum.class.isAssignableFrom(field.getType())) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Enum enumValue = (Enum) field.get(enumClass);
                                        if (enumValue.name().equalsIgnoreCase(String.valueOf(pathEntry.getValue()))) {
                                            mod.getSetting(pathEntry.getKey()).setValue(enumValue);
                                        }
                                    }
                                }
                            } else if(setting.getValue() instanceof Integer) {
                                mod.getSetting(pathEntry.getKey()).setValue(Integer.valueOf((int) Double.parseDouble(String.valueOf(pathEntry.getValue()))));
                            } else if(setting.getValue() instanceof Double) {
                                mod.getSetting(pathEntry.getKey()).setValue(Double.valueOf(String.valueOf(pathEntry.getValue())));
                            } else if(setting.getValue() instanceof Float) {
                                mod.getSetting(pathEntry.getKey()).setValue(Float.valueOf(String.valueOf(pathEntry.getValue())));
                            } else if(setting.getValue() instanceof Boolean) {
                                mod.getSetting(pathEntry.getKey()).setValue(Boolean.valueOf(String.valueOf(pathEntry.getValue())));
                            } else if(setting.getValue() instanceof String) {
                                mod.getSetting(pathEntry.getKey()).setValue(pathEntry.getValue());
                            }
                            //else {
                            //    mod.getSetting(pathEntry.getKey()).setValue(pathEntry.getValue());
                            //}
                        }
                        if (pathEntry.getKey().equalsIgnoreCase("Enabled")) {
                            if (mod.isEnabled() != Boolean.parseBoolean(String.valueOf(pathEntry.getValue()))) {
                                mod.toggle();
                            }
                            if (!mod.isEnabled() && mod.isAlwaysListening()) {
                                mod.toggle();
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                Dactyl.logger.info("Failed loading module " + mod.getName());
                exception.printStackTrace();
            }
        }
        return true;
    }

    public static void save(String configName, boolean isMainConfig) throws IOException {
        File dirFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl");
        if(!dirFile.exists()) {
            dirFile.mkdir();
        }
        ConfigUtil.createDirsIfNotExists(new String[] {"config", "config"+File.separator+(isMainConfig ? "Loaded Config" : configName)});
        for(Module mod : Dactyl.moduleManager.getModules()) {
            ConfigUtil.createDirIfNotExistsRaw(File.separator+"config"+File.separator+(isMainConfig ? "Loaded Config" : configName)+File.separator+mod.getCategory().toString()+File.separator);
            if(isMainConfig) {
                File moduleConfig = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"config"+File.separator+"Loaded Config"+File.separator+File.separator+mod.getCategory().toString()+File.separator+mod.getName()+".yml"+File.separator);
                ConfigUtil.createFileIfNotExistsRaw(moduleConfig);
                saveModuleConfig(moduleConfig, mod);
            } else {
                File moduleConfig = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"config"+File.separator+configName+File.separator+File.separator+mod.getCategory().toString()+File.separator+mod.getName()+".yml"+File.separator);
                ConfigUtil.createFileIfNotExistsRaw(moduleConfig);
                saveModuleConfig(moduleConfig, mod);
            }
        }
    }

    public static void saveModuleConfig(File file, Module module) throws IOException {
        ConfigUtil.clearFile(file);
        BufferedWriter bufferedWriter = ConfigUtil.makeWriter(file, true);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        yamlData.put("Enabled", module.isEnabled());
        for(Setting setting : module.getSettingsList()) {
            if(setting.getValue() instanceof Bind) {
                Map<String, Object> nestedKey = new HashMap<String, Object>();
                nestedKey.put("value", ((Bind)setting.getValue()).getKey());
                yamlData.put(setting.getName(), nestedKey);
            } else if(setting.getValue() instanceof Enum) {
                yamlData.put(setting.getName(), getEnumName((Enum)setting.getValue()));
            } else {
                yamlData.put(setting.getName(), setting.getValue());
            }
        }
        Yaml yaml = new Yaml();
        yaml.dump(yamlData, bufferedWriter);
        ConfigUtil.closeWriter(bufferedWriter);
    }

    public static void createIfNotExists(String configName, boolean isMainConfig) throws IOException {
        File dirFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl");
        if(!dirFile.exists()) {
            dirFile.mkdir();
        }
        File spammerDir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"spammer");
        if(!spammerDir.exists()) {
            spammerDir.mkdir();
        }
        File kitsDir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"kits");
        if(!kitsDir.exists()) {
            kitsDir.mkdir();
        }
        ConfigUtil.createDirsIfNotExists(new String[] {"config", "config"+File.separator+(isMainConfig ? "Loaded Config" : configName)});
        for(Module mod : Dactyl.moduleManager.getModules()) {
            ConfigUtil.createDirIfNotExistsRaw(File.separator+"config"+File.separator+(isMainConfig ? "Loaded Config" : configName)+File.separator+mod.getCategory().toString()+File.separator);
            if(isMainConfig) {
                File moduleConfig = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"config"+File.separator+"Loaded Config"+File.separator+File.separator+mod.getCategory().toString()+File.separator+mod.getName()+".yml"+File.separator);
                ConfigUtil.createFileIfNotExistsRaw(moduleConfig);
                initModuleConfig(moduleConfig, mod);
            } else {
                File moduleConfig = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"config"+File.separator+configName+File.separator+File.separator+mod.getCategory().toString()+File.separator+mod.getName()+".yml"+File.separator);
                ConfigUtil.createFileIfNotExistsRaw(moduleConfig);
                initModuleConfig(moduleConfig, mod);
            }
        }
    }

    public static void initModuleConfig(File file, Module module) throws IOException {
        Map<String, Object> alreadyCreated = new HashMap<>();
        InputStream configStream = new FileInputStream(file);
        Map<String, Map<String, Object>> yamlObj = new Yaml().load(configStream);
        if(yamlObj != null) {
            for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                if (module.getSettingNames() != null && (module.getSettingNames().contains(pathEntry.getKey()) || pathEntry.getKey().equalsIgnoreCase("Enabled"))) {
                    alreadyCreated.put(pathEntry.getKey(), pathEntry.getValue());
                }
            }
        }
        ConfigUtil.clearFile(file);
        BufferedWriter bufferedWriter = ConfigUtil.makeWriter(file, true);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        if(!alreadyCreated.containsKey("Enabled")) {
            yamlData.put("Enabled", module.isEnabled());
        }
        for (Map.Entry<String, Object> createdEntry : alreadyCreated.entrySet()) {
            yamlData.put(createdEntry.getKey(), createdEntry.getValue());
        }
        for(Setting setting : module.getSettingsList()) {
            if(!alreadyCreated.containsKey(setting.getName())) {
                if(setting.getValue() instanceof Bind) {
                    Map<String, Object> nestedKey = new HashMap<String, Object>();
                    nestedKey.put("value", ((Bind)setting.getValue()).getKey());
                    yamlData.put(setting.getName(), nestedKey);
                } else if(setting.getValue() instanceof Enum) {
                    yamlData.put(setting.getName(), getEnumName((Enum)setting.getValue()));
                } else {
                    yamlData.put(setting.getName(), setting.getValue());
                }
            }
        }
        Yaml yaml = new Yaml();
        yaml.dump(yamlData, bufferedWriter);
        ConfigUtil.closeWriter(bufferedWriter);
    }

    private static String getEnumName(Enum en) {
        return en.toString();
        //return en.name().toLowerCase().substring(0, 1).toUpperCase() + en.name().toLowerCase().substring(1);
    }

    private static final String sep = File.separator;
}
