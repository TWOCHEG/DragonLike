package com.purr.gui;

import com.purr.config.ConfigManager;
import com.purr.modules.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;

public class ClickGui {
    private static boolean key = false;
    private static final ConfigManager config = new ConfigManager("click_gui");

    public ClickGui(ModuleManager moduleManager) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            boolean isRightShiftPressed = InputUtil.isKeyPressed(
                client.getWindow().getHandle(),
                config.get("keybind", 344) // код 344 - правый шифт
            );
            Screen currentScreen = client.currentScreen;

            if (isRightShiftPressed && !key) {
                if (currentScreen instanceof ClickGuiScreen clickGuiScreen) {
                    clickGuiScreen.animReverse = true;
                } else if (currentScreen == null || currentScreen instanceof TitleScreen) {
                    client.setScreen(new ClickGuiScreen(client.currentScreen, moduleManager));
                }
            }
            key = isRightShiftPressed;
        });
    }
}
