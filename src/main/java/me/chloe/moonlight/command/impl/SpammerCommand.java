package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.command.Command;
import me.chloe.moonlight.module.impl.misc.Spammer;
import me.chloe.moonlight.util.ChatUtil;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;

public class SpammerCommand extends Command {
    public SpammerCommand() {
        super("Spammer", new String[] {"spammer"},"Changes the file for spammer");
    }

    @Override
    public void run(String[] args) {
        boolean isIncorrect = isIncorrect(args);

        if(isIncorrect) {
            handleIncorrectUsage();
            return;
        }

        handleSetFile(args);
    }

    private void handleSetFile(String[] args) {
        File spammerFile = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath(), File.separator+"Dactyl"+File.separator+"spammer"+File.separator+args[1]+".txt");
        if(!spammerFile.exists()) {
            ChatUtil.printMsg("&6[Spammer] &rFile does not exist!", true, false);
        } else {
            ArrayList<String> spammerMessages = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(spammerFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    spammerMessages.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Spammer.INSTANCE.spammerText = spammerMessages;
            Spammer.INSTANCE.reset();
        }
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Moonlight.commandManager.getPrefix() +"spammer <filename>", true, false);
    }

    private boolean isIncorrect(String[] args) {
        boolean incorrect = false;
        if(args.length < 2) {
            incorrect = true;
        }
        return incorrect;
    }
}
