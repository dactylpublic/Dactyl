package me.fluffy.dactyl.command.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.command.Command;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class GrabCommand extends Command {
    public GrabCommand() {
        super("Grab", new String[] {"grab"},"Grab information about the player.");
    }

    @Override
    public void run(String[] args) {
        boolean isIncorrect = isIncorrect(args);

        if(isIncorrect) {
            handleIncorrectUsage();
            return;
        }
        handleGrab(args);
    }

    private void handleGrab(String[] args) {
        if(Minecraft.getMinecraft().player == null) {
            return;
        }
        if(args[1].equalsIgnoreCase("coords")) {
            String xCoord = String.format("%.1f", Minecraft.getMinecraft().player.posX);
            String yCoord = String.format("%.1f", Minecraft.getMinecraft().player.posY);
            String zCoord = String.format("%.1f", Minecraft.getMinecraft().player.posZ);
            StringSelection stringSelection = new StringSelection("X: " + xCoord + " Y: " + yCoord + " Z: " + zCoord);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            ChatUtil.printMsg("Coords copied to clipboard.", true, false);
        }
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Dactyl.commandManager.getPrefix() +"grab <coords>", true, false);
    }

    private boolean isIncorrect(String[] args) {
        boolean incorrect = false;
        if(args.length < 2) {
            incorrect = true;
        } else {
            if(!(args[1].equalsIgnoreCase("coords"))) {
                incorrect = true;
            }
        }
        return incorrect;
    }
}
