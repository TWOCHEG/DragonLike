package pon.main.events.impl;

import pon.main.events.Event;

public class EventKeyRelease extends Event {
    private final int key;
    private final int scanCode;
    private final int modifiers;

    public EventKeyRelease(int key, int scanCode, int modifiers) {
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
