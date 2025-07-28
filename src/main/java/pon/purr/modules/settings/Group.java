package pon.purr.modules.settings;

import java.util.*;

public class Group {
    public boolean open;
    public final String name;
    public LinkedList<Setting> settings = new LinkedList<>();

    public Group(String name, Setting... settings) {
        this.name = name;
        for (Setting s : settings) {
            s.group = this;
            this.settings.add(s);
        }
    }
}