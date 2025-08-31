package pon.main.modules.ui;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.EventChangePlayerLook;
import pon.main.gui.ConfigsGui;
import pon.main.gui.FriendsGui;
import pon.main.gui.ModulesGui;
import pon.main.gui.components.CategoryArea;
import pon.main.gui.components.ChoseGuiArea;
import pon.main.gui.components.ConfigWindowArea;
import pon.main.modules.Parent;
import pon.main.modules.settings.*;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    public ColorSet theme = new ColorSet("theme", new Color(0, 0, 0), false);
    public ColorSet textColor = new ColorSet("text color", new Color(255, 255, 255), false);
    public Group colors = new Group("colors", theme, textColor).setType(Group.GroupType.Horizontal);

    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public Setting<String> image = new Setting<>(
        "image",
        new LinkedList<>(images.keySet())
    ).onSet((Setting<String> set) -> {
        if (!Objects.equals(set.getValue(), "none")) {
            texture = Identifier.of("main", getImages().get(set.getValue()));
        }
    });
    public Setting<Float> imgSize = new Setting<>(
        "image size",
        0.5f, 0.1f, 2.0f
    ).visibleProvider(m -> !image.getValue().equals("none"));

    public final Setting<Float> animSpeed = new Setting<>("animations speed", 0.5f, 0f, 1f);

    public final Setting<Boolean> showAreas = new Setting<>("show areas (debug)", false);

    public Identifier texture = Identifier.of("purr", images.get(image.getValue()));

    // render components
    public LinkedList<CategoryArea> categories = null;
    public Screen oldScreen = null;
    public ChoseGuiArea choseGuiArea = new ChoseGuiArea(ModulesGui.class, ConfigsGui.class, FriendsGui.class);
    public ConfigWindowArea configWindowArea = new ConfigWindowArea();

    public Gui() {
        super("click gui", Main.Categories.ui, GLFW.GLFW_KEY_RIGHT_SHIFT);
        setEnable(false);
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen instanceof TitleScreen || mc.currentScreen == null) {
            choseGuiArea.show = true;
            if (categories == null) {
                Map<Main.Categories, List<Parent>> modules = Main.MODULE_MANAGER.getForGui();
                categories = new LinkedList<>();
                for (Map.Entry<Main.Categories, List<Parent>> entry : modules.entrySet()) {
                    LinkedList<Parent> moduleList = new LinkedList<>(entry.getValue());
                    categories.add(new CategoryArea(moduleList, entry.getKey()));
                }
            } else {
                closeGuiAnimComponents();
            }
            oldScreen = mc.currentScreen;
            mc.setScreen(new ModulesGui());
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen != null) {
            choseGuiArea.show = false;
            choseGuiArea.returnToDefault();
            mc.currentScreen.close();
        }
    }

    @Override
    public void setEnable(boolean value) {
        setEnable(value, false);
    }

    @Override
    public void setKeybind(int code) {
        if (code != -1) {
            CONFIG.set("keybind", code);
            keybindCode = code;
        }
    }

    @EventHandler
    private void onChangeLook(EventChangePlayerLook e) {
        if (!mouseMove.getValue()) return;
        if (Parent.fullNullCheck()) return;
        if (mc.currentScreen instanceof ModulesGui gui) {
            gui.onChangeLook(e);
        } else if (mc.currentScreen instanceof ConfigsGui configsGui) {
            configsGui.onChangeLook(e);
        } else if (mc.currentScreen instanceof FriendsGui friendsGui) {
            friendsGui.onChangeLook(e);
        }
    }

    public void closeGuiAnimComponents() {
        for (CategoryArea categoryArea : categories) {
            categoryArea.show = false;
            categoryArea.showFactor = 0;
            categoryArea.updateAnim();
        }
        configWindowArea.showFactor = 0;
        configWindowArea.show = false;
        configWindowArea.resetCM();
    }
}
