package pon.main.modules.settings;

import java.util.*;

public class Group extends Setting<Setting> {
    public boolean open;

    public Group(String name, Setting... settings) {
        super(name, Arrays.stream(settings).toList());
        for (Setting s : getOptions()) {
            s.group = this;
        }
    }
    public Group(Setting defaultValue, String name, Setting... settings) {
        super(name, defaultValue, Arrays.stream(settings).toList());
        for (Setting s : getOptions()) {
            s.group = this;
        }
    }
}