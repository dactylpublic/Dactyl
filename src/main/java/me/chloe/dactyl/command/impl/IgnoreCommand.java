package me.chloe.dactyl.command.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.command.Command;

import java.util.ArrayList;

public class IgnoreCommand extends Command {
    public IgnoreCommand() {
        super("Ignore", new String[] {"ignore", "i"},"Ignores people");
    }
    public static ArrayList<String> ignoredUsers = new ArrayList<>();

    @Override
    public void run(String[] args) {
        boolean[] correction = setIncorrect(args);
        boolean incorrect = correction[0];
        boolean doAlias = correction[1];

        if(incorrect) {
            handleIncorrectUsage();
            return;
        }
        switch(args[1].toLowerCase()) {
            case "add":
                handleAddIgnored(args);
                break;
            case "del":
                handleRemoveIgnored(args);
                break;
            default:
                handleIncorrectUsage();
                break;
        }
    }


    private boolean[] setIncorrect(String[] args) {
        boolean incorrect = false;
        boolean doAlias = true;
        if(args.length < 2) {
            incorrect = true;
        }
        if(!incorrect) {
            if (args[1].equalsIgnoreCase("add")) {
                if (args.length < 4) {
                    if (args.length == 3) {
                        doAlias = false;
                    } else {
                        incorrect = true;
                    }
                }
            } else if (args[1].equalsIgnoreCase("del")) {
                if (args.length < 3) {
                    incorrect = true;
                }
            }
        }
        return new boolean[] {incorrect, doAlias};
    }

    private void handleIncorrectUsage() {
        ChatUtil.printMsg("&cIncorrect usage!", true, false);
        ChatUtil.printMsg("Usage: "+ Dactyl.commandManager.getPrefix() +"ignore <add/del> <username>", true, false);
    }

    private void handleAddIgnored(String[] args) {
        if(!ignoredUsers.contains(args[2])) {
            ignoredUsers.add(args[2]);
            ChatUtil.printMsg("&7" + args[2] + " added to the ignore list.", true, true);
        } else {
            ChatUtil.printMsg("&7" + args[2] + " is already on the ignore list.", true, true);
        }
    }

    private void handleRemoveIgnored(String[] args) {
        if(ignoredUsers.contains(args[2])) {
            ignoredUsers.remove(args[2]);
            ChatUtil.printMsg("&7Unignored " + args[2] + ".", true, true);
        } else {
            ChatUtil.printMsg("&7" + args[2] + " is not on the ignore list.", true, true);
        }
    }
}
