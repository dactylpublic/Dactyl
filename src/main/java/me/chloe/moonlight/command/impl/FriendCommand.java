package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.command.Command;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("Friend", new String[] {"friend", "f", "fr"},"Manage friends using this command.");
    }

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
                handleAddFriend(args);
                break;
            case "del":
                handleRemoveFriend(args);
                break;
            default:
                handleCheckFriend(args);
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
        ChatUtil.printMsg("Usage: "+ Moonlight.commandManager.getPrefix() +"friend <user/add/del> (<username>)", true, false);
    }

    private void handleAddFriend(String[] args) {
        if(!Moonlight.friendManager.isFriend(args[2])) {
            Moonlight.friendManager.addFriend(args[2]);
            ChatUtil.printMsg("&7Friend " + args[2] + " added.", true, true);
        } else {
            ChatUtil.printMsg("&7" + args[2] + " is already on the friends list.", true, true);
        }
    }

    private void handleRemoveFriend(String[] args) {
        if(Moonlight.friendManager.isFriend(args[2])) {
            Moonlight.friendManager.removeFriend(args[2]);
            ChatUtil.printMsg("&7Friend " + args[2] + " removed.", true, true);
        } else {
            ChatUtil.printMsg("&7" + args[2] + " is not on the friends list.", true, true);
        }
    }

    private void handleCheckFriend(String[] args) {
        String message = Moonlight.friendManager.isFriend(args[1]) ? "&7" + args[1] + " is friended." : "&7" + args[1] + " is not friended.";
        ChatUtil.printMsg(message, true, true);
    }
}
