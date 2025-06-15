package purr.purr.modules.ui;

import purr.purr.gui.ConfigsGui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import purr.purr.gui.ClickGui;
import purr.purr.modules.Parent;

public class ConfigMenu extends Parent {
    public boolean show = false;
    public ConfigMenu() {
        super("configs", "ui");
        init();
    }

    public void init() {
        if (getEnable()) {
            setEnable(false);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (enable && !show) {
                boolean retr = false;
                if (client.currentScreen instanceof ClickGui screen) {
                    screen.closeGui();
                    retr = true;
                }
                if (!retr) {
                    client.setScreen(new ConfigsGui(client.currentScreen, this));
                    show = true;
                }
            } else if (!enable && client.currentScreen instanceof ConfigsGui screen) {
                screen.closeGui();
            }
        });
    }
}
