package me.chloe.moonlight.listener;

public class Listener {
    private final String name;
    private final String desc;
    public Listener(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }
}
