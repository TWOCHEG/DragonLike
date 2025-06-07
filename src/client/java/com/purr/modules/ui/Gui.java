package com.purr.modules.ui;

import com.purr.gui.ClickGui;
import com.purr.modules.*;
import com.purr.modules.settings.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;

import java.util.*;

public class Gui extends Parent {
    public final Map<String, String> images = Map.of(
        "none", "none",
        "furry", "textures/gui/furry.png",
        "billy", "textures/gui/billy.png",
        "nya", "textures/gui/nya.png",
        "sonic", "textures/gui/sonic.png",
        "shayrma", "textures/gui/shayrma.png",
        "furry2", "textures/gui/furry2.png",
        "skala", "textures/gui/skala.png",
        "smalik", "textures/gui/smalik.png"
    );

    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public ListSetting<String> image = new ListSetting<>(
        "image",
        new LinkedList<>(images.keySet())
    );

    private static boolean key = false;

    public Gui() {
        super("click gui", "click_gui", "ui");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null) {
                boolean isRightShiftPressed = InputUtil.isKeyPressed(
                    client.getWindow().getHandle(),
                    config.get("keybind", 344) // код 344 - правый шифт
                );
                Screen currentScreen = client.currentScreen;

                if (isRightShiftPressed && !key) {
                    if (currentScreen instanceof ClickGui clickGuiScreen) {
                        clickGuiScreen.animReverse = true;
                    } else if (currentScreen == null || currentScreen instanceof TitleScreen) {
                        client.setScreen(new ClickGui(client.currentScreen, moduleManager));
                    }
                }
                key = isRightShiftPressed;
            }
        });
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void setKeybind(int code) {
        if (code != -1) {
            config.set("keybind", code);
            keybindCode = code;
        }
    }

    @Override
    public void setEnable(boolean value) {
        enable = false;
    }

    @Override
    public boolean getEnable() {
        return false;
    }
}
