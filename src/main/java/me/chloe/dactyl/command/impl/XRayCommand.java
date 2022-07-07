package me.chloe.dactyl.command.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.command.Command;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

public class XRayCommand extends Command {
    public XRayCommand() {
        super("XRay", new String[] {"xray", "xr", "x"},"Manage xray blocks using this command.");
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
                handleAddBlock(args);
                break;
            case "del":
                handleRemoveBlock(args);
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
        ChatUtil.printMsg("Usage: "+ Dactyl.commandManager.getPrefix() +"xray <add/del> <blockname>", true, false);
    }

    private void handleAddBlock(String[] args) {
        if(Block.getBlockFromName(args[2]) == null) {
            ChatUtil.printMsg("&cBlock does not exist.", true, false);
            return;
        }
        if(!Dactyl.xRayManager.hasBlock(Block.getBlockFromName(args[2]))) {
            Dactyl.xRayManager.addBlock(args[2]);
            ChatUtil.printMsg("&7Block " + Block.getBlockFromName(args[2]) + " added.", true, true);
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        } else {
            ChatUtil.printMsg("&7Block " + Block.getBlockFromName(args[2]) + " is already in the XRay list.", true, true);
        }
    }

    private void handleRemoveBlock(String[] args) {
        if(Dactyl.xRayManager.hasBlock(Block.getBlockFromName(args[2]))) {
            Dactyl.xRayManager.removeBlock(args[2]);
            ChatUtil.printMsg("&7Block " + Block.getBlockFromName(args[2]) + " removed.", true, true);
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        } else {
            ChatUtil.printMsg("&7Block " + Block.getBlockFromName(args[2]) + " is not in the XRay list.", true, true);
        }
    }
}
