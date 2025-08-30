package pon.main.events.impl;

import pon.main.events.Event;

import java.nio.file.Path;

public class OnChangeConfig extends Event {
    private Path oldConfig;
    private Path currentConfig;
    public OnChangeConfig(Path oldConfig, Path currentConfig) {
        this.oldConfig = oldConfig;
        this.currentConfig = currentConfig;
    }

    public Path getOld() {
        return oldConfig;
    }
    public Path getCurrent() {
        return currentConfig;
    }
}
