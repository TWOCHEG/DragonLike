package pon.purr.modules.settings;

import pon.purr.utils.Color;

public class ColorSettings extends Setting<Integer> {
    public ColorSettings(String name, int defaultValue) {
        super(name, defaultValue);
    }

    public int r() {
        return Color.fromInt(getValue()).getFirst();
    }
    public int g() {
        return Color.fromInt(getValue()).get(1);
    }
    public int b() {
        return Color.fromInt(getValue()).get(2);
    }
    public int a() {
        return Color.fromInt(getValue()).get(3);
    }
}
