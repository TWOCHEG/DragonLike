package com.skylong.modules.settings;

import com.skylong.modules.Parent;

public class Setting<T> {
    private final String key;
    private final String name;
    private T value;
    private final T defaultValue;
    public T min, max;
    private Parent module;

    public Setting(String name, String key, T defaultValue) {
        this.key = key;
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    // Конструктор для чисел с границами
    public Setting(String name, String key, T defaultValue, T min, T max) {
        this(name, key, defaultValue);
        this.min = min;
        this.max = max;
    }

    // Геттеры/сеттеры
    public String getKey() { return key; }
    public String getName() { return name; }
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        System.out.println(value);
        this.value = value;
        module.setValue(key, value);
    }

    public void setModule(Parent module) {
        this.module = module;
    }
}

