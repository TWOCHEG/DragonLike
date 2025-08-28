package pon.main.modules.settings;

import pon.main.modules.Parent;
import pon.main.utils.EnumConverter;

import java.util.Objects;
import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    protected T value = null;
    public T min, max;
    public Parent module;
    public Group group = null;
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
    public static Enum get(Enum clazz) {
        int index = EnumConverter.currentEnum(clazz);
        for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
            Enum e = clazz.getClass().getEnumConstants()[i];
            if (i != index + 1) continue;
            return e;
        }
        return clazz.getClass().getEnumConstants()[0];
    }
    public void setValue(T value) {
        if (this.module != null && !Objects.equals(value, getValue())) {
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

