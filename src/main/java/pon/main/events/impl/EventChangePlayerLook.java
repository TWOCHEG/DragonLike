package pon.main.events.impl;

import pon.main.events.Event;

public class EventChangePlayerLook extends Event {
    public double cursorDeltaX;
    public double cursorDeltaY;

    public EventChangePlayerLook(double cursorDeltaX, double cursorDeltaY) {
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }
}
