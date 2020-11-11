package me.fluffy.dactyl.command;

import me.fluffy.dactyl.util.Bind;
import org.lwjgl.input.Keyboard;

public class Command {
    private String name;
    private String displayName;
    private String description;
    private String[] aliases;
    private Bind bind;

    public Command(String name, String[] aliases, String description) {
        this.name = name;
        this.displayName = name;
        this.description = description;
        this.aliases = aliases;
        this.bind = new Bind(Keyboard.KEY_NONE);
    }

    public void run(String[] args) {

    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Bind getBind() {
        return this.bind;
    }

    public void setBind(Bind bind) {
        this.bind = bind;
    }

    public String getDescription() { return this.description; }

    public void setDescription(String description) { this.description = description; }

    public String[] getAliases() { return this.aliases; }

    public void setAliases(String[] aliases) { this.aliases = aliases; }
}
