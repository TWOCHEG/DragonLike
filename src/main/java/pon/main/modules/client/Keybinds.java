package pon.main.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.TitleScreen;
import pon.main.Main;
import pon.main.events.impl.EventKeyPress;
import pon.main.managers.Managers;
import pon.main.modules.Parent;

public class Keybinds extends Parent {

    public Keybinds() {
        super(null, null);
    }

    @EventHandler
    private void keyPress(EventKeyPress e) {
        if (
            mc.currentScreen != null &&
            !(mc.currentScreen instanceof TitleScreen) &&
            e.getKey() != Managers.MODULE_MANAGER.getModule(Gui.class).getKeybind()
        ) return;
        if (e.getModifiers() == 5) return;  // смена языка

        for (Parent module : Managers.MODULE_MANAGER.getModules()) {
            module.onKey(e.getKey());
        }
    }
}
