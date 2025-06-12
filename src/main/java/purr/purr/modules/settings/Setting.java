package purr.purr.modules.settings;

import purr.purr.modules.Parent;

import java.util.*;

public class Setting<T> {
    private final String name;
    private T value = null;
    public T min, max;
    private Parent module;
    private Group group = null;

    private Setting visibleClass = null;
    private List<Object> visibleValues = null;
    private boolean visibleBlacklist = false;

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
        if (value != null) {
            this.value = value;
            module.setValue(name, value);
        }
    }

    public void setModule(Parent module) {
        this.module = module;
        this.value = (T) module.getValue(name, defaultValue);
    }

    public Setting addToGroup(Group group) {
        this.group = group;
        return this;
    }

    public Setting visibleIf(Setting setClass, Object... values) {
        this.visibleClass = setClass;
        this.visibleValues = Arrays.asList(values);
        return this;
    }
    public Setting visibleIfBlacklist(Setting setClass, Object... values) {
        this.visibleClass = setClass;
        this.visibleValues = Arrays.asList(values);
        this.visibleBlacklist = true;
        return this;
    }
    public boolean getVisible() {
        if (visibleValues != null) {
            if (!visibleBlacklist) {
                return visibleValues.contains(visibleClass.getValue());
            } else return !visibleValues.contains(visibleClass.getValue());
        }
        return true;
    }

    public Group getGroup() {
        return group;
    }
}

