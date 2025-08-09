package pon.purr.modules.settings;

import pon.purr.gui.components.NumberSettingsArea;
import pon.purr.gui.components.RenderArea;
import pon.purr.modules.Parent;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private T value = null;
    public T min, max;
    public Parent module;
    public SettingsGroup group = null;
    private Predicate<T> visibility;

    public T defaultValue;

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
        if (this.module != null) {
            module.onUpdate(this);
            this.value = value;
            module.setValue(name, value);
        }
    }

    public void setModule(Parent module) {
        this.module = module;
        this.value = (T) module.getValue(name, defaultValue);
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
}

