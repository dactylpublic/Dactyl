package me.fluffy.dactyl.command.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.command.Command;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClearPopsCommand extends Command {
    public ClearPopsCommand() {
        super("ClearPops", new String[] {"cp", "clearp", "clear", "clearpops", "clearpop", "popclear"},"Clears the client side totem pops (listed above the users nametag)");
    }

    @Override
    public void run(String[] args) {
        boolean isIncorrect = isIncorrect(args);

        if(isIncorrect) {
            handleIncorrectUsage();
            return;
        }
        if(!Dactyl.totemManager.hasPops(String.valueOf(args[1]))) {
            ChatUtil.printMsg("&cThat user has no pops!", true, false);
            return;
        }
        handleClear(args);
    }

    private void handleClear(String[] args) {
        if(Minecraft.getMinecraft().player == null) {
            return;
        }
        Dactyl.totemManager.clearUser(String.valueOf(args[1]));
        ChatUtil.printMsg("&aTotem pops clear for user " + String.valueOf(args[1]), true, false);
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Dactyl.commandManager.getPrefix() +"clearpops <username>", true, false);
        ChatUtil.printMsg("Aliases: cp/clearp/clear/clearpops/clearpop/popclear", true, false);
    }

    private boolean isIncorrect(String[] args) {
        boolean incorrect = false;
        if(args.length < 2) {
            incorrect = true;
        }
        return incorrect;
    }
}
