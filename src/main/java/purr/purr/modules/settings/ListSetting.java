package purr.purr.modules.settings;

import java.util.*;

public class ListSetting<E> extends Setting<E> {
    private final List<E> options;
    private Class<? extends Enum> enumClass = null;

    public ListSetting(String name, List<E> options) {
        super(name, options.getFirst());
        this.options = options;
    }
    public ListSetting(String name, Class<? extends Enum> options) {
        super(name, (E) options.getEnumConstants()[0].toString());
        this.enumClass = options;
        LinkedList<String> lst = new LinkedList<>();
        for (Enum cnts : options.getEnumConstants()) {
            lst.add(cnts.toString());
        }
        this.options = (List<E>) lst;
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
