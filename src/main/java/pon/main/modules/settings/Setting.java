package pon.main.modules.settings;

import pon.main.modules.Parent;
import pon.main.utils.EnumHelper;

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
    public Consumer<Setting<T>> onChange;

    public T defaultValue;

    private List<T> options = null;
    private int optionIndex;

    public Setting(T defaultValue) {
        this(
            defaultValue instanceof Enum
                    ? EnumHelper.getNameFromEnum((Enum<?>) defaultValue)
                    : defaultValue.toString(),
            defaultValue
        );
    }
    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;

        if (defaultValue instanceof Enum) {
            this.options = (List<T>) Arrays.asList(EnumHelper.getConstaints((Enum<?>) defaultValue));
            this.optionIndex = this.options.indexOf(defaultValue);
        } else {
            this.value = defaultValue;
        }
    }
    public Setting(String name, List<T> options) {
        this(
            options.getFirst(),
            name,
            options
        );
    }
    public Setting(T defaultValue, String name, List<T> options) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.optionIndex = options.indexOf(defaultValue);
        this.options = options;
    }
    public Setting(String name, T... options) {
        this(
            name,
            Arrays.stream(options).toList()
        );
    }
    public Setting(T defaultValue, String name, T... options) {
        this(defaultValue, name, Arrays.stream(options).toList());
    }
    public Setting(String name, T defaultValue, T min, T max) {
        this(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public String getName() { return name; }

    public T getValue() {
        if (isList()) {
            return options.get(Math.clamp(optionIndex, 0, options.size() - 1));
        } else {
            return value != null ? value : defaultValue;
        }
    }

    public float getPow2Value() {
        if (value instanceof Float)
            return (float) value * (float) value;

        if (value instanceof Integer)
            return (int) value * (int) value;

        return 0;
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

            if (onChange != null) {
                onChange.accept(this);
            }
        }
    }

    public int getIndex() {
        return optionIndex;
    }

    public void setIndex(int i) {
        if (i > options.size() - 1) {
            i = 0;
        } else if (i < 0) {
            i = options.size() - 1;
        }
        i = Math.clamp(i, 0, options.size() - 1);
        setValue(getOptions().get(i));
    }

    public boolean isList() {
        return options != null;
    }

    public List<T> getOptions() {
        return options;
    }

    public void init(Parent module) {
        this.module = module;
        T v = (T) module.getConfig().get(name, isList() ? optionIndex : defaultValue);
        if (isList()) {
            setIndex((Integer) v);
        } else {
            setValue(v);
        }
    }

    public Setting<T> visible(Predicate<T> visibility) {
        this.visibility = visibility;
        return this;
    }
    public Setting<T> onChange(Consumer<Setting<T>> onChange) {
        this.onChange = onChange;
        return this;
    }

    public boolean getVisible() {
        if (visibility == null)
            return true;

        return visibility.test(getValue());
    }
}