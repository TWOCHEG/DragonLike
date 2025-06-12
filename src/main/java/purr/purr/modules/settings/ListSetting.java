package purr.purr.modules.settings;

import java.util.*;

public class ListSetting<E> extends Setting<E> {
    private final List<E> options;
    private Class<? extends Enum> enumClass = null;

    public ListSetting(String name, List<E> options) {
        super(name, options.getFirst());
        this.options = new ArrayList<>(options);

        if (!this.options.isEmpty() && this.options.get(0) instanceof Enum) {
            Enum firstEnum = (Enum) this.options.get(0);
            this.enumClass = firstEnum.getDeclaringClass();
            List<String> stringOptions = this.options.stream()
                    .map(Object::toString)
                    .toList();
            this.options.clear();
            this.options.addAll((List<E>) stringOptions); // Safe cast if E is String
        }
    }

    public Enum<?> getEnumValue() {
        if (enumClass == null) return null;
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.toString().equals(getValue()))
                .findFirst()
                .orElse(null);
    }

    public List<E> getOptions() { return options; }

    public Enum getEValue() {
        for (Enum cnts : enumClass.getEnumConstants()) {
            if (cnts.toString() == getValue()) {
                return cnts;
            }
        }
        return null;
    }
}
