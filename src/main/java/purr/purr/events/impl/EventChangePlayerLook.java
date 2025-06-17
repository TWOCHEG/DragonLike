package purr.purr.events.impl;

import purr.purr.events.Event;

public class EventChangePlayerLook extends Event {
    public double cursorDeltaX;
    public double cursorDeltaY;

    public EventChangePlayerLook(double cursorDeltaX, double cursorDeltaY) {
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }
}
