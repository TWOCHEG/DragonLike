package pon.main.modules.settings;

import java.util.*;

public class SetsList<E> extends Setting<E> {
    private final java.util.List<E> options;

    public SetsList(String name, List<E> options) {
        super(name, options.getFirst());
        this.options = options;
    }
    public SetsList(String name, E defaultValue, List<E> options) {
        super(name, defaultValue);
        this.options = options;
    }

    public java.util.List<E> getOptions() { return options; }

    public boolean equals(Enum v) {
        return v.name().toLowerCase().equals(getValue().toString().toLowerCase());
    }
}
