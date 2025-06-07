package com.purr.events.impl;

import net.minecraft.text.ClickEvent;

public abstract class ClientClickEvent implements ClickEvent {
    public ClientClickEvent(Action action, String value) {
        super();
    }
}