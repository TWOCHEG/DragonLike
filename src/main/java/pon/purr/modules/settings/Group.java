package pon.purr.modules.settings;

import java.util.*;

public class Group<Settings> extends ListSetting<Settings> {
    public boolean open;

    public Group(String name, Setting... settings) {
        super(name, (List<Settings>) Arrays.stream(settings).toList());
        List<Settings> list = new LinkedList<>();
        for (Setting s : settings) {
            list.add((Settings) s);
            s.group = this;
        }
    }
}