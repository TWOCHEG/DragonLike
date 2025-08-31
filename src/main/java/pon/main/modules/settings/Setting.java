package pon.main.modules.settings;

import pon.main.modules.Parent;
import pon.main.utils.EnumConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    protected T value = null;
    public T min, max;
    public Parent module;
    public Group group = null;
    private Predicate<T> visibility;
    private Consumer<Setting<T>> onSet;

    public T defaultValue;

    private List<T> options = null;
    private int optionIndex;
    public Setting(T defaultValue) {
        this(
            defaultValue instanceof Enum
                ? EnumConverter.getNameFromEnum((Enum<?>) defaultValue)
                : defaultValue.toString(),
            defaultValue
        );
    }

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;

        if (defaultValue instanceof Enum) {
            this.options = (List<T>) Arrays.asList(EnumConverter.getConstaints((Enum<?>) defaultValue));
            this.optionIndex = this.options.indexOf(defaultValue);
        } else {
            this.value = defaultValue;
        }
    }

    public Setting(String name, List<T> options) {
        this.name = name;
        this.optionIndex = 0;
        this.options = options;
    }

    public Setting(String name, T defaultValue, List<T> options) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.optionIndex = options.indexOf(defaultValue);
        this.options = options;
    }
    public Setting(String name, T... options) {
        this.name = name;
        List<T> lst = Arrays.stream(options).toList();
        this.optionIndex = 0;
        this.options = lst;
    }
    public Setting(String name, T defaultValue, T... options) {
        this.name = name;
        this.defaultValue = defaultValue;
        List<T> lst = Arrays.stream(options).toList();
        this.optionIndex = lst.indexOf(defaultValue);
        this.options = lst;
    }

    public Setting(String name, T defaultValue, T min, T max) {
        this(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public String getName() { return name; }

    public T getValue() {
        if (isList()) {
            return options.get(optionIndex);
        } else {
            return value != null ? value : defaultValue;
        }
    }

    public void setValue(T value) {
        if (this.module != null && !Objects.equals(value, getValue())) {
            module.onSettingUpdate(this);

            if (isList()) {
                this.optionIndex = options.indexOf(value);
                module.setValue(name, optionIndex);
            } else {
                this.value = value;
                module.setValue(name, value);
            }

            if (onSet != null) {
                 onSet.accept(this);
            }
        }
    }

    public int getIndex() {
        return optionIndex;
    }

    public void setIndex(int i) {
        setValue(getOptions().get(i));
    }

    public boolean isList() {
        return options != null;
    }

    public List<T> getOptions() {
        return options;
    }

    public void setModule(Parent module) {
        this.module = module;
        this.value = module.getValue(name, defaultValue);
    }

    public Setting<T> visibleProvider(Predicate<T> visibility) {
        this.visibility = visibility;
        return this;
    }
    public Setting<T> onSet(Consumer<Setting<T>> onSet) {
        this.onSet = onSet;
        return this;
    }

    public boolean getVisible() {
        if (visibility == null)
            return true;

        return visibility.test(getValue());
    }
}