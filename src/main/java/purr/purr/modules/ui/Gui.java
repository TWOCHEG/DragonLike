package purr.purr.modules.ui;

import org.lwjgl.glfw.GLFW;
import purr.purr.events.impl.EventTick;
import purr.purr.gui.ClickGui;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.*;

import java.util.*;

public class Gui extends Parent {
    public static final Map<String, String> images = new LinkedHashMap<>();
    static {
        images.put("none", "none");
        images.put("furry", "textures/gui/furry.png");
        images.put("billy", "textures/gui/billy.png");
        images.put("nya", "textures/gui/nya.png");
        images.put("sonic", "textures/gui/sonic.png");
        images.put("shayrma", "textures/gui/shayrma.png");
        images.put("furry2", "textures/gui/furry2.png");
        images.put("skala", "textures/gui/skala.png");
        images.put("smalik", "textures/gui/smalik.png");
    }
    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public ListSetting<String> image = new ListSetting<>(
        "image",
        new LinkedList<>(images.keySet())
    );
    public Setting<Float> imgSize = new Setting<>(
        "image size",
        0.5f, 0.1f, 2.0f
    ).visibleIf(m -> !image.getValue().equals("none"));
    public final Setting<Boolean> setBg = new Setting<>("settings bg", true);
    public final Setting<Integer> setBgAlpha = new Setting<>("settings bg A", 150, 0, 255).visibleIf(m -> setBg.getValue());
    public final Setting<Float> settingsScale = new Setting<>("settings scale", 0.8f, 0.1f, 2f).visibleIf(m -> setBg.getValue());
    public final Group animations = new Group("animations");
    public final Setting<Boolean> animEnable = new Setting<>("enable animations", true).addToGroup(animations);
    public final Setting<Boolean> runProcess = new Setting<>("run separate process", false).addToGroup(animations);
    public final Setting<Integer> animSpeed = new Setting<>("animations speed", 30, 1, 100).visibleIf(m -> animEnable.getValue()).addToGroup(animations);

    private static boolean keyWasPressed = false;

    public Gui() {
        super("click gui", "ui");
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (client == null) return;

        boolean isKeyPressed = InputUtil.isKeyPressed(
            client.getWindow().getHandle(),
            getKeybind() != -1 ? getKeybind() : GLFW.GLFW_KEY_RIGHT_SHIFT
        );

        if (isKeyPressed && !keyWasPressed) {
            if (client.currentScreen instanceof ClickGui) {
                ((ClickGui) client.currentScreen).closeGui();
            } else {
                client.setScreen(new ClickGui(client.currentScreen, moduleManager, this));
            }
        }
        keyWasPressed = isKeyPressed;
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
