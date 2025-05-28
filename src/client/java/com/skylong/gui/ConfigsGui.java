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

    public static final String hintsText = "click on title - open path\nCONTROL + M - create\nMOUSE RIGHT - delete";

    private Identifier texturePlus = Identifier.of("skylong", "textures/gui/plus.png");
    private String texturePlusName = "plus";
    private Identifier textureDel = Identifier.of("skylong", "textures/gui/delete.png");
    private String textureDelName = "Del";

    private String guiTextId = "openPath";

    public Map<String, Integer> clickAnimations = new HashMap<>();
    public Map<String, Boolean> clickAnimReverse = new HashMap<>();

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

        float hintsScale = 0.7f;
        int alpha = 150 * (int) animPercent / 100;
        int colorHints = GetColor.getColor(255, 255, 255, alpha);
        int xHints = 5;
        int yHints = height - textRenderer.fontHeight - 17;
        String[] lines = hintsText.split("\n");
        context.getMatrices().push();
        context.getMatrices().translate(xHints, yHints, 2);
        context.getMatrices().scale(hintsScale, hintsScale, 2);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            context.drawTextWithShadow(textRenderer, Text.literal(line), 0, i * (textRenderer.fontHeight + 2), colorHints);
        }
        context.getMatrices().pop();

        float screenWidth = context.getScaledWindowWidth();
        int imgSize = 16;
        int animDiff = (imgSize / 4);
        float imgX = 5;
        float imgY = ((imgSize + imgX) * animPercent / 100) - imgSize;
        // === КНОПКА PLUS ===
        int plusAnim = clickAnimations.getOrDefault(texturePlusName, 0);
        int plusSize = animDiff * plusAnim / 100;
        if (plusSize == animDiff) {
            clickAnimReverse.put(texturePlusName, true);
        }
        int plusTotalSize = imgSize + plusSize;
        float plusOffset = plusSize / 2.0f;

        context.getMatrices().push();
        context.getMatrices().translate(imgX - plusOffset, imgY - plusOffset, 2);
        context.drawTexture(
            RenderLayer::getGuiTextured,
            texturePlus,
            0, 0,
            0, 0,
            plusTotalSize, plusTotalSize,
            plusTotalSize, plusTotalSize
        );
        context.getMatrices().pop();
        moduleAreas.add(new ModuleArea(texturePlusName, imgX, imgY, imgSize, imgSize));

        // === КНОПКА DELETE ===
        int delAnim = clickAnimations.getOrDefault(textureDelName, 0);
        int delSize = animDiff * delAnim / 100;
        if (delSize == animDiff) {
            clickAnimReverse.put(textureDelName, true);
        }
        int delTotalSize = imgSize + delSize;
        float delOffset = delSize / 2.0f;

        float delX = imgX + imgSize + imgX;

        context.getMatrices().push();
        context.getMatrices().translate(delX - delOffset, imgY - delOffset, 2);
        context.drawTexture(
            RenderLayer::getGuiTextured,
            textureDel,
            0, 0,
            0, 0,
            delTotalSize, delTotalSize,
            delTotalSize, delTotalSize
        );
        context.getMatrices().pop();
        moduleAreas.add(new ModuleArea(textureDelName, delX, imgY, imgSize, imgSize));

        Text display = Text.literal("configs gui").formatted(Formatting.BOLD);
        int textWidth = textRenderer.getWidth(display);
        float titleX = (screenWidth / 2) - ((float) textWidth / 2);
        float titleY = 10 + (50 * (100 -  animPercent) / 100);
        context.getMatrices().push();
        context.getMatrices().translate(
            titleX,
            titleY,
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
        moduleAreas.add(new ModuleArea(guiTextId, titleX, titleY, textWidth, textRenderer.fontHeight));

        List<Path> filesList = config.configFiles();
        if (oldListpaths.isEmpty()) {
            oldListpaths = filesList;
        }

        float startX = 20;
        float startY = 50;
        float x = startX;
        float y = startY;
        float lineHeight = textRenderer.fontHeight + 10;

        for (Path path : filesList) {
            String name = path.getFileName().toString();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                name = name.substring(0, dotIndex);
            }
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
                x,
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
        }

        oldListpaths = filesList;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof String str) {
                if (str.equals(texturePlusName)) {
                    clickAnimations.put(texturePlusName, 1);
                    onCreate();
                } else if (str.equals(textureDelName)) {
                    clickAnimations.put(textureDelName, 1);
                    onDelete(config.getActiveConfig());
                } else if (str.equals(guiTextId)) {
                    config.openConfigDir();
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
        if (keyCode == GLFW.GLFW_KEY_M) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                onCreate();
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
        // анимация открытия/закрытия
        if (!animReverse && animPercent < 100) {
            animPercent += Math.max(0.1f, (animDiff * (100 - animPercent)) / 100);
        } else if (animReverse && animPercent > 0) {
            animPercent -= Math.max(0.1f, (animDiff * animPercent) / 100);
        }
        animPercent = Math.clamp(animPercent, 0, 100);

        // анимация клика
        if (!clickAnimations.isEmpty()) {
            Iterator<String> it = clickAnimations.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                int percent = clickAnimations.get(name);
                boolean reverse = clickAnimReverse.getOrDefault(name, false);

                if (!reverse && percent < 100) {
                    percent += Math.max(1, ((animDiff * 3) * (100 - percent)) / 100);
                } else if (reverse && percent > 0) {
                    percent -= Math.max(1, ((animDiff * 3) * percent) / 100);
                }

                percent = Math.clamp(percent, 0, 100);
                clickAnimations.put(name, percent);

                if (reverse && percent == 0) {
                    it.remove();
                    clickAnimReverse.remove(name);
                }
            }
        }
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

    private void onRename(Path path, String newNane) {
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
