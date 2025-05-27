package com.skylong.gui;

import com.skylong.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Identifier;

import com.skylong.modules.ui.ConfigMenu;
import com.skylong.utils.GetColor;

import java.nio.file.Path;
import java.util.*;

public class ConfigsGui extends Screen {
    private final Screen previous;
    public ConfigMenu configMenu;
    private final ConfigManager config = new ConfigManager("click_gui");

    private Identifier texture = Identifier.of("skylong", "textures/gui/plus.png");
    private String textureName = "plus";
    private ArrayList<Integer> keyDeleteList = new ArrayList<>(List.of(GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE));

    private List<Path> oldListpaths = new ArrayList<>();

    public float animPercent = 0;
    public boolean animReverse = false;

    public double lastMouseX = 0;
    public double lastMouseY = 0;

    public List<ModuleArea> moduleAreas = new ArrayList<>();

    public ConfigsGui(Screen previous, ConfigMenu configMenu) {
        super(Text.literal("Configs Gui"));
        this.previous = previous;
        this.configMenu = configMenu;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (previous != null) {
            previous.render(context, mouseX, mouseY, delta);
        }

        moduleAreas.clear();

        animHandler(10);
        if (animReverse && animPercent == 0) {
            configMenu.show = false;
            client.setScreen(null);
            return;
        }

        float startY = 50;

        // Фон с градиентом
        int alphaTop = 100 * (int) animPercent / 100;
        int alphaBottom = 200 * (int) animPercent / 100;
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 1);
        context.getMatrices().scale(1, 1, 1);
        context.fillGradient(0, 0, width, height,
            GetColor.getColor(0, 0, 0, alphaTop),
            GetColor.getColor(0, 0, 0, alphaBottom)
        );
        context.getMatrices().pop();

        float screenWidth = context.getScaledWindowWidth();
        int imgSize = 15;

        float imgX = ((20 + imgSize) * animPercent / 100) - imgSize;
        float imgY = startY - (textRenderer.fontHeight / 2);

        context.getMatrices().push();
        context.getMatrices().translate(imgX, imgY, 2);
        context.drawTexture(
            RenderLayer::getGuiTextured,
            texture,
            0,
            0,
            0, 0,
            imgSize,
            imgSize,
            imgSize,
            imgSize
        );
        context.getMatrices().pop();

        moduleAreas.add(new ModuleArea(textureName, imgX, imgY, imgSize, imgSize));

        Text display = Text.literal("configs gui").formatted(Formatting.BOLD);
        int textWidth = textRenderer.getWidth(display);

        context.getMatrices().push();
        context.getMatrices().translate(
            (screenWidth / 2) - ((float) textWidth / 2),
            10 + (50 * (100 -  animPercent) / 100),
            2
        );
        context.getMatrices().scale(1, 1, 2);
        context.drawTextWithShadow(
            textRenderer,
            display,
            0, 0,
            GetColor.getColor(255, 255, 255, (int) (255 * animPercent / 100))
        );
        context.getMatrices().pop();

        List<Path> filesList = config.configFiles();
        if (oldListpaths.isEmpty()) {
            oldListpaths = filesList;
        }

        int i = 0;
        float startX = 20;
        float x = startX;
        float y = startY;
        float lineHeight = textRenderer.fontHeight + 10;

        for (Path path : filesList) {
            String name = path.getFileName().toString();
            Path p = config.getActiveConfig();
            boolean active = path.equals(p);
            Text text = active ? Text.literal(name).formatted(Formatting.BOLD) : Text.literal(name);
            int w = textRenderer.getWidth(text);

            // Перенос на новую строку, если не влезает
            if (x + w > screenWidth - 20) {
                x = startX;
                y += lineHeight;
            }

            // Рисуем через матрицы
            context.getMatrices().push();
            context.getMatrices().translate(
                i < 1 ? x += 30 : x,
                y + (50 * (100 -  animPercent) / 100),
                2
            );
            context.drawTextWithShadow(
                textRenderer,
                text,
                0, 0,
                GetColor.getColor(255, 255, 255, (int) (255 * animPercent / 100))
            );
            context.getMatrices().pop();
            moduleAreas.add(new ModuleArea(path, x, y, w, textRenderer.fontHeight));

            x += w + 15;
            i += 1;
        }

        oldListpaths = filesList;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof String str) {
                System.out.println(1);
                if (str.equals(textureName)) {
                    onCreate();
                }
            } else if (obj instanceof Path p) {
                onSetActive(p);
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof Path p) {
                onDelete(p);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyDeleteList.contains(keyCode)) {

            MinecraftClient client = MinecraftClient.getInstance();
            double scaleFactor = client.getWindow().getScaleFactor();
            double mouseX = client.mouse.getX() / scaleFactor;
            double mouseY = client.mouse.getY() / scaleFactor;

            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof Path p) {
                onDelete(p);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (client != null && client.player != null && animPercent >= 100 && !animReverse && config.get("mouse_move", true)) {
            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            if (lastMouseX == 0 && lastMouseY == 0) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return;
            }

            float sensitivity = 0.05f;

            float yaw = client.player.getYaw() + (float) (deltaX * sensitivity);
            float pitch = Math.clamp(client.player.getPitch() + (float) (deltaY * sensitivity), -89.0f, 89.0f);

            client.player.setYaw(yaw);
            client.player.setPitch(pitch);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        super.mouseMoved(mouseX, mouseY);
    }

    public void close() {
        configMenu.setEnable(false);
        animReverse = true;
    }

    public void animHandler(int animDiff) {
        // Анимация открытия/закрытия
        if (!animReverse && animPercent < 100) {
            animPercent += Math.max(0.1f, (animDiff * (100 - animPercent)) / 100);
        } else if (animReverse && animPercent > 0) {
            animPercent -= Math.max(0.1f, (animDiff * animPercent) / 100);
        }
        animPercent = Math.clamp(animPercent, 0, 100);
    }

    private void onDelete(Path path) {
        config.deleteConfig(path);
    }

    private void onCreate() {
        config.createConfig();
    }

    private void onSetActive(Path path) {
        config.setActiveConfig(path);
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
