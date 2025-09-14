package pon.main.modules.settings;

import java.util.ArrayList;
import java.util.List;

public class EnableableGroup extends Setting<Boolean> {
    public boolean open;
    public List<Setting> settings = new ArrayList<>();

    public EnableableGroup(String name, Setting... settings) {
        super(name, false);
        for (Setting s : settings) {
            s.group = this;
            this.settings.add(s);
        }
    }
    public EnableableGroup(String name, boolean enable, Setting... settings) {
        super(name, enable);
        for (Setting s : settings) {
            s.group = this;
            this.settings.add(s);
        }
    }
}
