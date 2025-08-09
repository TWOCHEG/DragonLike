package pon.purr.events.impl;

import pon.purr.events.Event;

public class EventResizeScreen extends Event {
    public int width, height, scaleFactor;

    public EventResizeScreen(int width, int height, int scaleFactor) {
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
    }
}
