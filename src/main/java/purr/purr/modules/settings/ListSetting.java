package purr.purr.modules.settings;

import java.util.*;

public class ListSetting<E> extends Setting<E> {
    private final List<E> options;

    public ListSetting(String name, List<E> options) {
        super(name, options.getFirst());
        this.options = options;
    }

    public List<E> getOptions() { return options; }
}
