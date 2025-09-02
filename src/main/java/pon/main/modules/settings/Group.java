package pon.main.modules.settings;

import java.util.*;

public class Group extends Setting<Setting> {
    public boolean open;
    public GroupType groupType = GroupType.Vertical;

    public Group(String name, Setting... settings) {
        super(name, settings);
        for (Setting s : getOptions()) {
            s.group = this;
        }
    }
    public Group(Setting defaultValue, String name, Setting... settings) {
        super(defaultValue, name, settings);
        for (Setting s : getOptions()) {
            s.group = this;
        }
    }

    public Group setType(GroupType groupType) {
        this.groupType = groupType;
        return this;
    }

    public enum GroupType {
        Vertical, Horizontal
    }
}