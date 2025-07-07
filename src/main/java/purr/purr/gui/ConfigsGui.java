// зона 51
package purr.purr.gui;

import purr.purr.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import purr.purr.modules.ui.ConfigMenu;
import purr.purr.utils.RGB;

import java.nio.file.Path;
import java.util.*;

public class ConfigsGui extends Screen {
    private final Screen previous;
    private final ConfigMenu configMenu;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final ConfigManager config = new ConfigManager("click_gui");

    private final List<ModuleArea> moduleAreas = new ArrayList<>();

    public ConfigsGui(Screen previous, ConfigMenu configMenu) {
        super(Text.literal("Configs Gui"));
        this.previous = previous;
        this.configMenu = configMenu;
    }

    public void closeGui() {
        configMenu.setEnable(false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Text msg = Text.literal("this gui in progress");
        context.drawTextWithShadow(
            textRenderer,
            msg,
            (mc.currentScreen.width / 2) - (textRenderer.getWidth(msg) / 2),
            (mc.currentScreen.height / 2) - (textRenderer.fontHeight / 2),
            RGB.getColor(255, 255, 255)
        );
        super.render(context, mouseX, mouseY, delta);
    }

    private void onDelete(Path path) {
        config.deleteConfig(path);
    }

    private Path onCreate() {
        return config.createConfig();
    }

    private void onSetActive(Path path) {
        config.setActiveConfig(path);
    }

    private void onRename(Path path, String newNane) {
        if (path == null) {
            path = onCreate();
        }
        config.renameConfig(path, newNane);
    }

    private Object getModuleUnderMouse(int mouseX, int mouseY) {
        for (int i = moduleAreas.size() - 1; i >= 0; i--) {
            ModuleArea area = moduleAreas.get(i);
            if (mouseX >= area.x
                    && mouseX <= area.x + area.width
                    && mouseY >= area.y
                    && mouseY <= area.y + area.height) {
                return area.module;
            }
        }
        return null;
    }

    private String getPathName(Path path) {
        if (path == null) return "default";
        String name = path.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }
        return name;
    }

    private static class ModuleArea {
        private final Object module;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public ModuleArea(Object module, float x, float y, float width, float height) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
