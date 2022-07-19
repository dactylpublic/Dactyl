package me.chloe.moonlight.config;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.Map;

public class ConfigUtil {
    public static File createFileIfNotExists(String name, String exstension) throws IOException {
        File dir = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+File.separator);
        if(!dir.exists()) {
            dir.mkdir();
        }
        File namedFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+File.separator+name+"."+exstension);
        if(!namedFile.exists()) {
            namedFile.createNewFile();
        }
        return namedFile;
    }

    public static void createFileIfNotExistsRaw(File file) throws IOException {
        if(!file.exists()) {
            file.createNewFile();
        }
    }

    public static void createDirsIfNotExists(String[] directories) throws IOException {
        for(String directory : directories) {
            File dirFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+File.separator+directory);
            if(!dirFile.exists()) {
                dirFile.mkdir();
            }
        }
    }

    public static void createDirIfNotExists(String directory) throws IOException {
        File dirFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+File.separator+directory+File.separator);
        if(!dirFile.exists()) {
            dirFile.mkdir();
        }
    }

    public static void createDirIfNotExistsRaw(String directory) throws IOException {
        File dirFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+directory);
        if(!dirFile.exists()) {
            dirFile.mkdir();
        }
    }

    public static BufferedWriter makeWriter(File file, boolean append) throws IOException {
        return new BufferedWriter(new FileWriter(file, true));
    }

    public static void appendToFile(BufferedWriter bufferedWriter, String text) throws IOException {
        if(bufferedWriter != null) {
            bufferedWriter.append(text+"\r\n");
        }
        assert bufferedWriter != null;
        bufferedWriter.close();
    }

    public static void writeToFile(BufferedWriter bufferedWriter, String text) throws IOException {
        if(bufferedWriter != null) {
            bufferedWriter.write(text+"\r\n");
        }
    }

    public static void closeWriter(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.close();
    }

    public static void clearFile(File file) throws IOException {
        PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
        printWriter.write("");
        printWriter.close();
    }

    public static File makeClientDirFile(String path) {
        return new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Moonlight"+File.separator+path+File.separator);
    }


    public static class YAMLConfig {
        private Map<String, String> paths;
        private String packagePrefix;

        public Map<String, String> getPaths() {
            return paths;
        }

        public void setPaths(Map<String, String> paths) {
            this.paths = paths;
        }

        public String getPackagePrefix() {
            return packagePrefix;
        }

        public void setPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
        }

        @Override
        public String toString() {
            return "YAMLConfig [paths=" + paths + "]";
        }
    }
}
