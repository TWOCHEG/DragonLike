package pon.purr.modules.settings;

import java.util.*;

public class SettingsGroup<Settings> extends ListSetting<Settings> {
    public boolean open;

    public SettingsGroup(String name, Setting... settings) {
        super(name, (List<Settings>) Arrays.stream(settings).toList());
        List<Settings> list = new LinkedList<>();
        for (Setting s : settings) {
            list.add((Settings) s);
            s.group = this;
        }
    }
}