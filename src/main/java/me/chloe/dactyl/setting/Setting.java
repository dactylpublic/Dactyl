package me.chloe.dactyl.setting;

import me.chloe.dactyl.module.Module;

import java.util.function.Predicate;

public class Setting<T> {

    private final String name;
    private final String desc;
    private Module module;

    private T value, minimum, maximum;

    private Predicate<Boolean> visibility;

    public Setting(String name, T value, String desc) {
        this.name = name;
        this.desc = desc;
        this.value = value;
    }

    public Setting(String name, T value, T minimum, T maximum, String desc) {
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Setting(String name, T value, Predicate<Boolean> visibility, String desc) {
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.visibility = visibility;
    }

    public Setting(String name, T value, T minimum, T maximum, Predicate<Boolean> visibility,  String desc) {
        this.name = name;
        this.desc = desc;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.visibility = visibility;
    }
    public Setting(String name, T value) {
        this.name = name;
        this.desc = "";
        this.value = value;
    }

    public Setting(String name, T value, T minimum, T maximum) {
        this.name = name;
        this.desc = "";
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Setting(String name, T value, Predicate<Boolean> visibility) {
        this.name = name;
        this.desc = "";
        this.value = value;
        this.visibility = visibility;
    }

    public Setting(String name, T value, T minimum, T maximum, Predicate<Boolean> visibility) {
        this.name = name;
        this.desc = "";
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.visibility = visibility;
    }

    public T getValue() {
        return this.value;
    }

    public T getMinimum() {
        return this.minimum;
    }

    public T getMaximum() {
        return this.maximum;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isVisible() {
        if(this.visibility == null) return true;
        return this.visibility.test(true);
    }
}
