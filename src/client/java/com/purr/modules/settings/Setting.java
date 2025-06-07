package com.purr.modules.settings;

import com.purr.modules.Parent;

public class Setting<T> {
    private final String name;
    private T value;
    public T min, max;
    private Parent module;

    private T defaultValue;

    public Setting(String name, T defaultValue) {
        System.out.println(name);
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public Setting(String name, T defaultValue, T min, T max) {
        this(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public String getName() { return name; }
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
        module.setValue(name, value);
    }

    public void setModule(Parent module) {
        this.module = module;
        this.value = (T) module.getValue(name, defaultValue);
    }
}

