package com.purr.events;

import meteordevelopment.orbit.*;
import com.purr.events.classes.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Listener {
    private IEventBus bus;

    public Listener(IEventBus bus) {
        this.bus = bus;
        onTick();
    }

    private void onTick() {
        ClientTickEvents.START_CLIENT_TICK.register(context -> {
            bus.post(new TickEvent());
        });
    }
}
