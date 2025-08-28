package pon.main.events.impl;

import pon.main.events.Event;

public class EventPostPlayerUpdate extends Event {
    private int iterations;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int in) {
        iterations = in;
    }
}