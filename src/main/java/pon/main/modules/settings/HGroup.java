package pon.main.modules.settings;

import java.util.List;

public class HGroup extends Group {
    public HGroup(String name, Setting currentSetting, Setting... settings) {
        super(currentSetting, name, settings);
    }

    @Override
    public List<Setting> getOptions() {
        return super.getOptions();
    }

    public void setValue(Setting value) {
        super.setValue(getOptions().indexOf(value));
    }

    public Setting getValue() {
        return getOptions().get((int) super.getValue());
    }

    public int getIndex() {
        return (int) super.getValue();
    }
}
