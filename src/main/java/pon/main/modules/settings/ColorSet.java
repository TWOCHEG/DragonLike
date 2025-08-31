package pon.main.modules.settings;

import pon.main.utils.ColorUtils;

import java.awt.*;

public class ColorSet extends Setting<Integer> {
    public boolean hasAlpha = true;
    public ColorSet(String name, int defaultValue) {
        super(name, defaultValue);
    }
    public ColorSet(String name, int defaultValue, boolean hasAlpha) {
        super(name, defaultValue);
        this.hasAlpha = hasAlpha;
    }
    public ColorSet(String name, Color defaultValue) {
        super(name, defaultValue.getRGB());
    }
    public ColorSet(String name, Color defaultValue, boolean hasAlpha) {
        super(name, defaultValue.getRGB());
        this.hasAlpha = hasAlpha;
    }

    public java.awt.Color color() {
        return new java.awt.Color(getValue(), ColorUtils.hasAlpha(getValue()));
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

    public void setColor(java.awt.Color color) {
        setValue(color.getRGB());
    }
}
