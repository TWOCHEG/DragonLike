package pon.purr.modules.ui;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.TitleScreen;
import pon.purr.Purr;
import pon.purr.events.impl.EventResizeScreen;
import pon.purr.gui.ModulesGui;
import pon.purr.gui.components.CategoryArea;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.*;
import pon.purr.utils.ColorUtils;

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

    public ColorSettings theme = new ColorSettings("theme", ColorUtils.fromRGB(0, 0, 0));
    public ColorSettings textColor = new ColorSettings("text color", ColorUtils.fromRGB(255, 255, 255));
    public Setting<Boolean> mouseMove = new Setting<>("mouse move", true);
    public ListSetting<String> image = new ListSetting<>(
        "image",
        new LinkedList<>(images.keySet())
    );
    public Setting<Float> imgSize = new Setting<>(
        "image size",
        0.5f, 0.1f, 2.0f
    ).visibleIf(m -> !image.getValue().equals("none"));

    public final Setting<Boolean> animEnable = new Setting<>("enable animations", true);
    public final Setting<Integer> animSpeed = new Setting<>("animations speed", 30, 1, 100).visibleIf(m -> animEnable.getValue());
    public final SettingsGroup animations = new SettingsGroup(
        "animations",
        animEnable,
        animSpeed
    );
    public final Setting<Boolean> showAreas = new Setting<>("show areas (debug)", false);

    public int imageWidth = 0;
    public int imageHeight = 0;
    public Identifier texture = Identifier.of("purr", images.get(image.getValue()));

    public LinkedList<CategoryArea> categories = null;

    public Gui() {
        super("click gui", Purr.Categories.ui);
        getConfig().set("keybind", getConfig().get("keybind", 344));
        enable = false;
    }

    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen instanceof TitleScreen || mc.currentScreen == null) {
            if (categories == null) {
                Map<Purr.Categories, List<Parent>> modules = Purr.moduleManager.getModules();
                categories = new LinkedList<>();
                for (Map.Entry<Purr.Categories, List<Parent>> entry : modules.entrySet()) {
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

    public List<Integer> getImageSize(Identifier texture) {
        if (imageWidth == 0 && imageHeight == 0) {
            updateImageSize(texture, mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }
        return Arrays.asList(imageWidth, imageHeight);
    }
}
