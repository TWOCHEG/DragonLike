package pon.main.events.impl;

import pon.main.events.Event;

public class EventFramebufferResize extends Event {
    public int width, height;

    public EventFramebufferResize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
