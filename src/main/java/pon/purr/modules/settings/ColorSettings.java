package pon.purr.modules.settings;

import pon.purr.utils.ColorUtils;

public class ColorSettings extends Setting<Integer> {
    public ColorSettings(String name, int defaultValue) {
        super(name, defaultValue);
    }

    public int r() {
        return ColorUtils.fromInt(getValue()).getRed();
    }
    public int g() {
        return ColorUtils.fromInt(getValue()).getGreen();
    }
    public int b() {
        return ColorUtils.fromInt(getValue()).getBlue();
    }
    public int a() {
        return ColorUtils.fromInt(getValue()).getAlpha();
    }
}
