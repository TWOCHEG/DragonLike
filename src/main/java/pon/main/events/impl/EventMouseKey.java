package pon.main.events.impl;

import pon.main.events.Event;

public class EventMouseKey extends Event {
    private int button;
    private double x, y;
    private int action;

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public EventMouseKey(int b, int action, double x, double y) {
        button = b;
        this.x = x;
        this.y = y;
        this.action = action;
    }
}
