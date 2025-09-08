package pon.main.events.impl;

import pon.main.events.Event;

public class EventMouseScroll extends Event {
    private double x, y, horizontal, vertical;

    public double getH() {
        return horizontal;
    }
    public double getV() {
        return vertical;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public EventMouseScroll(double x, double y, double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.x = x;
        this.y = y;
    }
}
