package pon.main.modules.ui;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.EventChangePlayerLook;
import pon.main.events.impl.EventResizeScreen;
import pon.main.gui.ModulesGui;
import pon.main.gui.components.CategoryArea;
import pon.main.modules.Parent;
import pon.main.modules.settings.*;
import pon.main.modules.settings.SetsList;
import pon.main.utils.ColorUtils;

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

    public ColorSet theme = new ColorSet("theme", ColorUtils.fromRGB(0, 0, 0), false);
    public ColorSet textColor = new ColorSet("text color", ColorUtils.fromRGB(255, 255, 255), false);
    public HGroup colors = new HGroup("colors", theme, theme, textColor);

    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public SetsList<String> image = new SetsList<>(
        "image",
        new LinkedList<>(images.keySet())
    );
    public Setting<Float> imgSize = new Setting<>(
        "image size",
        0.5f, 0.1f, 2.0f
    ).visibleIf(m -> !image.getValue().equals("none"));

    public final Setting<Float> animSpeed = new Setting<>("animations speed", 0.5f, 0f, 1f);

    public final Setting<Boolean> showAreas = new Setting<>("show areas (debug)", false);

    public int imageWidth = 0;
    public int imageHeight = 0;
    public Identifier texture = Identifier.of("purr", images.get(image.getValue()));

    public LinkedList<CategoryArea> categories = null;

    public Gui() {
        super("click gui", Main.Categories.ui);
        getConfig().set("keybind", getConfig().get("keybind", GLFW.GLFW_KEY_RIGHT_SHIFT));
        enable = false;
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen instanceof TitleScreen || mc.currentScreen == null) {
            if (categories == null) {
                Map<Main.Categories, java.util.List<Parent>> modules = Main.moduleManager.getForGui();
                categories = new LinkedList<>();
                for (Map.Entry<Main.Categories, java.util.List<Parent>> entry : modules.entrySet()) {
                    LinkedList<Parent> moduleList = new LinkedList<>(entry.getValue());
                    categories.add(new CategoryArea(moduleList, entry.getKey()));
                }
            }
            mc.setScreen(new ModulesGui(mc.currentScreen, this));
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof ModulesGui modulesGui) {
            modulesGui.closeGui();
        }
    }

    @Override
    public void setEnable(boolean value) {
        setEnable(value, false);
    }

    @Override
    public void setKeybind(int code) {
        if (code != -1) {
            config.set("keybind", code);
            keybindCode = code;
        }
    }

    @Override
    public void onUpdate(Setting setting) {
        if (setting.equals(image) || setting.equals(imgSize)) {
            if (!Objects.equals(image.getValue(), "none")) {
                texture = Identifier.of("purr", getImages().get(image.getValue()));
                updateImageSize(texture, mc.getWindow().getWidth(), mc.getWindow().getHeight());
            }
        }
    }

    @EventHandler
    private void onChangeLook(EventChangePlayerLook e) {
        if (Parent.fullNullCheck()) return;
        if (mc.currentScreen instanceof ModulesGui gui) {
            gui.onChangeLook(e);
        }
    }

    @EventHandler
    private void onScreenResize(EventResizeScreen e) {
        updateImageSize(texture, e.width, e.height);
    }

    public void updateImageSize(Identifier texture, int screenWidth, int screenHeight) {
//        Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(texture);
//        NativeImage nativeImage;
//        try {
//            nativeImage = NativeImage.read(resource.get().getInputStream());
//        } catch (IOException e) {
//            return;
//        }
//
//        float imageWidth = nativeImage.getWidth();
//        float imageHeight = nativeImage.getHeight();
//
//        nativeImage.close();
//
//        float screenMin = Math.min(screenWidth, screenHeight);
//        float fixedSize = screenMin * imgSize.getValue();
//
//        float scale = Math.min(fixedSize / imageWidth, fixedSize / imageHeight);
//
//        this.imageWidth = (int) (imageWidth * scale);
//        this.imageHeight = (int) (imageHeight * scale);

        this.imageWidth = 100;
        this.imageHeight = 100;
    }

    public java.util.List<Integer> getImageSize(Identifier texture) {
        if (imageWidth == 0 && imageHeight == 0) {
            updateImageSize(texture, mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }
        return Arrays.asList(imageWidth, imageHeight);
    }
}
