package purr.purr.events;

import meteordevelopment.orbit.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import purr.purr.events.impl.TickEvent;

public class Listener {
    private IEventBus eventBus;

    public Listener(IEventBus bus) {
        this.eventBus = bus;
        onTick();
    }

    private void onTick() {
        ClientTickEvents.START_CLIENT_TICK.register(context -> {
            eventBus.post(new TickEvent());
        });
    }
}
