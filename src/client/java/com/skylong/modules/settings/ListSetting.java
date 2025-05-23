package com.skylong.modules.settings;

import java.util.*;

public class ListSetting<E> extends Setting<E> {
    private final List<E> options;
    private boolean expanded;
    public ListSetting(String name, String key, E defaultValue, List<E> options) {
        super(name, key, defaultValue);
        this.options = options;
    }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean exp) { this.expanded = exp; }
    public List<E> getOptions() { return options; }
}
