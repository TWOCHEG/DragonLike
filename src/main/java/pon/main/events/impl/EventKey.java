package pon.main.events.impl;

public class EventKey {
    private final int key;
    private final int scanCode;
    private final int modifiers;

    public EventKey(int key, int scanCode, int modifiers) {
        this.key = key;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
    }

    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }

    public int getModifiers() {
        return modifiers;
    }
}
