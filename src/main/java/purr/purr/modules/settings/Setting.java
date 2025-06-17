package purr.purr.modules.settings;

import purr.purr.modules.Parent;

import java.util.*;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private T value = null;
    public T min, max;
    private Parent module;
    private Group group = null;

    private Predicate<T> visibility;

    private T defaultValue;

    public Setting(String name, T defaultValue) {
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
        return value != null ? value : defaultValue;
    }
    public void setValue(T value) {
        if (this.value != null) {
            module.onUpdate(this);
            this.value = value;
            module.setValue(name, value);
        }
    }

    public void setModule(Parent module) {
        this.module = module;
        this.value = (T) module.getValue(name, defaultValue);
    }

    public Setting<T> addToGroup(Group group) {
        this.group = group;
        return this;
    }

    public Setting<T> visibleIf(Predicate<T> visibility) {
        this.visibility = visibility;
        return this;
    }

    public boolean getVisible() {
        if (visibility == null)
            return true;

        return visibility.test(getValue());
    }

    public Group getGroup() {
        return group;
    }
}

