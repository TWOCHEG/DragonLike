package pon.main.modules.settings;

import meteordevelopment.orbit.EventHandler;
import pon.main.Main;
import pon.main.events.impl.EventKeyPress;
import pon.main.events.impl.EventTick;

public class KeyButton extends Setting<Integer> {
    private Runnable task = null;

    public KeyButton(String name, int defaultValue) {
        super(name, defaultValue);
        Main.EVENT_BUS.subscribe(this);
    }
    public KeyButton(String name, int defaultValue, Runnable task) {
        super(name, defaultValue);
        this.task = task;
        Main.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onKey(EventKeyPress e) {
        if (task != null && e.getKey() == getValue()) {
            task.run();
        }
    }
}
