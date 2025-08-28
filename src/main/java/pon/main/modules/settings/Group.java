package pon.main.modules.settings;

import java.util.*;

public class Group extends SetsList {
    public boolean open;

    public Group(String name, Setting... settings) {
        super(name, Arrays.stream(settings).toList());
        List<Setting> list = new LinkedList<>();
        for (Setting s : settings) {
            list.add(s);
            s.group = this;
        }
    }
    public Group(Setting defaultValue, String name, Setting... settings) {
        super(name, Arrays.stream(settings).toList().indexOf(defaultValue), Arrays.stream(settings).toList());
        List<Setting> list = new LinkedList<>();
        for (Setting s : settings) {
            list.add(s);
            s.group = this;
        }
    }
}