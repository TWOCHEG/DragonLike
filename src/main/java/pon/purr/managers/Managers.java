package pon.purr.managers;

import pon.purr.Purr;
import pon.purr.managers.impl.*;

public class Managers {
    public static final PlayerManager PLAYER = new PlayerManager();
    public static final AsyncManager ASYNC = new AsyncManager();
    public static final TelemetryManager TELEMETRY = new TelemetryManager();

    public static void init() {
        subscribe();
    }

    public static void subscribe() {
        Purr.EVENT_BUS.subscribe(PLAYER);
        Purr.EVENT_BUS.subscribe(ASYNC);
        Purr.EVENT_BUS.subscribe(TELEMETRY);
    }
}
