package pon.main.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.EventResizeScreen;
import pon.main.gui.ConfigsGui;
import pon.main.gui.FriendsGui;
import pon.main.gui.HudGui;
import pon.main.gui.ModulesGui;
import pon.main.gui.components.CategoryArea;
import pon.main.gui.components.ChoseGuiArea;
import pon.main.gui.components.ConfigWindowArea;
import pon.main.gui.components.FriendsWindowArea;
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

    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public Setting<String> image = new Setting<>(
        "image",
        new LinkedList<>(images.keySet())
    ).onChange((Setting<String> set) -> {
        if (!Objects.equals(set.getValue(), images.keySet().stream().toList().getFirst())) {
            texture = Identifier.of("main", getImages().get(set.getValue()));
            updateImageSize();
        } else {
            texture = null;
        }
    });
    public Setting<Float> imgScale = new Setting<>(
        "image scale",
        0.5f, 0.1f, 1.0f
    ).visible(m -> !image.getValue().equals("none"))
    .onChange((s) -> updateImageSize());

    public final Setting<Float> animSpeed = new Setting<>("animations speed", 0.5f, 0f, 1f);

    public final Setting<Boolean> showAreas = new Setting<>("show areas (debug)", false);

    public Identifier texture = Identifier.of("main", images.get(image.getValue()));
    public int imageWidth = 0, imageHeight = 0;

    // render components
    public LinkedList<CategoryArea> categories = null;
    public Screen oldScreen = null;
    public ChoseGuiArea choseGuiArea = new ChoseGuiArea(ModulesGui.class, ConfigsGui.class, FriendsGui.class, HudGui.class);
    public ConfigWindowArea configWindowArea = new ConfigWindowArea();
    public FriendsWindowArea friendsWindowArea = new FriendsWindowArea();

    public Gui() {
        super("click gui", Main.Categories.client, GLFW.GLFW_KEY_RIGHT_SHIFT);
        setEnable(false, false);
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void onEnable() {
        updateImageSize();
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
                resetGuiAnimComponents();
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

    public void resetGuiAnimComponents() {
        for (CategoryArea categoryArea : categories) {
            categoryArea.show = false;
            categoryArea.showFactor = 0;
            categoryArea.updateAnim();
        }
        configWindowArea.showFactor = 0;
        configWindowArea.show = false;
        configWindowArea.resetCM();
        friendsWindowArea.show = false;
        friendsWindowArea.showFactor = 0;
        friendsWindowArea.resetCM();
    }

    @EventHandler
    private void onResize(EventResizeScreen e) {
        updateImageSize();
    }

    public void updateImageSize() {
        if (texture == null) return;
        try {
            Optional<Resource> resource = mc.getResourceManager().getResource(texture);
            try (NativeImage nativeImage = NativeImage.read(resource.get().getInputStream())) {

                float targetHeight = mc.getWindow().getHeight() / 2f;
                float baseScale = targetHeight / nativeImage.getHeight();
                float scale = baseScale * imgScale.getValue();

                this.imageWidth = (int) (nativeImage.getWidth() * scale);
                this.imageHeight = (int) (nativeImage.getHeight() * scale);
            }
        } catch (Exception ignored) {}
    }
}
