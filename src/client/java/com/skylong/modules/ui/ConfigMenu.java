package com.skylong.modules.ui;

import com.skylong.gui.ConfigsGui;
import com.skylong.modules.*;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;

public class ConfigMenu extends Parent {
    public boolean show = false;
    public ConfigMenu() {
        super("configs", "config_menu", "ui");
        init();
    }

    public void init() {
        if (getEnable()) {
            setEnable(false);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (enable && !show) {
                client.setScreen(null);
                client.setScreen(new ConfigsGui(client.currentScreen, this));
                show = true;
            }
        });
    }
}
