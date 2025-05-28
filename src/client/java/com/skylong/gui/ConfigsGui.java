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
    private final ConfigMenu configMenu;
    private final ConfigManager config = new ConfigManager("click_gui");

    private static final String hintsText = "click on title - open path\nR - edit name\nM - create\nBACKSPACE | X | DEL - delete";

    private final Identifier texturePlus = Identifier.of("skylong", "textures/gui/plus.png");
    private final String texturePlusName = "plus";
    private final Identifier textureDel = Identifier.of("skylong", "textures/gui/delete.png");
    private final String textureDelName = "Del";
    private final Identifier textureEdit = Identifier.of("skylong", "textures/gui/edit.png");
    private final String textureEditName = "Edit";

    private final String guiTextId = "openPath";

    private final Map<String, Integer> clickAnimations = new HashMap<>();
    private final Map<String, Boolean> clickAnimReverse = new HashMap<>();

    private final ArrayList<Integer> keyDeleteList = new ArrayList<>(List.of(GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE));

    private List<Path> oldListpaths = new ArrayList<>();

    private float animInput = 0;
    private boolean animInputReverse = true;
    private String inputText = "";

    private float animPercent = 0;
    public boolean animReverse = false;

    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private final List<ModuleArea> moduleAreas = new ArrayList<>();

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

        float screenWidth = context.getScaledWindowWidth();
        float screenHeight = context.getScaledWindowHeight();

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

        // подсказки
        String[] lines = hintsText.split("\n");
        float hintsScale = 0.7f;
        int alpha = 150 * (int) animPercent / 100;
        int colorHints = GetColor.getColor(255, 255, 255, alpha);
        int xHints = 5;
        float yHints = screenHeight - (textRenderer.fontHeight * lines.length);
        context.getMatrices().push();
        context.getMatrices().translate(xHints, yHints, 2);
        context.getMatrices().scale(hintsScale, hintsScale, 2);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            context.drawTextWithShadow(textRenderer, Text.literal(line), 0, i * (textRenderer.fontHeight + 2), colorHints);
        }
        context.getMatrices().pop();

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
        // === КНОПКА EDIT ===
        int editAnim = clickAnimations.getOrDefault(textureEditName, 0);
        int editSize = animDiff * editAnim / 100;
        if (editSize == animDiff) {
            clickAnimReverse.put(textureEditName, true);
        }
        int editTotalSize = imgSize + editSize;
        float editOffset = editSize / 2.0f;

        float editX = 2 * (imgX + imgSize) + imgX;

        context.getMatrices().push();
        context.getMatrices().translate(editX - editOffset, imgY - editOffset, 2);
        context.drawTexture(
            RenderLayer::getGuiTextured,
            textureEdit,
            0, 0,
            0, 0,
            editTotalSize, editTotalSize,
            editTotalSize, editTotalSize
        );
        context.getMatrices().pop();
        moduleAreas.add(new ModuleArea(textureEditName, editX, imgY, imgSize, imgSize));

        Text display = Text.literal("configs").formatted(Formatting.BOLD);
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

        if (animInput > 1) {
            // Фон с градиентом
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 3);
            context.getMatrices().scale(1, 1, 3);
            context.fillGradient(0, 0, width, height,
                GetColor.getColor(0, 0, 0, 200 * (int) animInput / 100),
                GetColor.getColor(0, 0, 0, 220 * (int) animInput / 100)
            );
            context.getMatrices().pop();

            String name = inputText.isEmpty() ? "..." : inputText;
            Text inputText = Text.literal(name);
            context.getMatrices().push();
            context.getMatrices().translate(
                (screenWidth / 2) - textRenderer.getWidth(inputText),
                (screenHeight / 2) - textRenderer.fontHeight,
                4
            );
            context.getMatrices().scale(2, 2, 4);
            context.drawTextWithShadow(
                textRenderer,
                inputText,
                0, 0,
                GetColor.getColor(255, 255, 255, (int) (255 * animInput / 100))
            );
            context.getMatrices().pop();
        }
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
                } else if (str.equals(textureEditName)) {
                    clickAnimations.put(textureEditName, 1);
                    animInputReverse = false;
                } else if (str.equals(guiTextId)) {
                    config.openConfigDir();
                }
            } else if (obj instanceof Path p) {
                onSetActive(p);
            }
            return true;
        }
        animInputReverse = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!animInputReverse) {
            inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (animInput > 20 && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_M) {
            onCreate();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R) {
            animInputReverse = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && !animInputReverse) {
            animInputReverse = true;
            onRename(config.getActiveConfig(), inputText);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !animInputReverse) {
            animInputReverse = true;
            return true;
        }
        if (keyDeleteList.contains(keyCode)) {
            onDelete(config.getActiveConfig());
            return true;
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

        // анимация ввода
        if (!animInputReverse && animInput < 100) {
            animInput += Math.max(0.1f, (animDiff * (100 - animInput)) / 100);
        } else if (animInputReverse && animInput > 0) {
            animInput -= Math.max(0.1f, (animDiff * animInput) / 100);
        }
        animInput = Math.clamp(animInput, 0, 100);
        if (animInput < 1) {
            inputText = "";
        }

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
