package pon.main.events.impl;

import pon.main.events.Event;

public class EventMouseMove extends Event {
    public double x, y, deltaX, deltaY;
    public EventMouseMove(double x, double y, double deltaX, double deltaY) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}
