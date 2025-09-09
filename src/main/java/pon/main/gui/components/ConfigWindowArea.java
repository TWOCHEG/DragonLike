package pon.main.gui.components;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.OnChangeConfig;
import pon.main.gui.ConfigsGui;
import pon.main.managers.Managers;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigWindowArea extends RenderArea {
    public boolean show = false;

    private ContextMenu cm = null;
    private final int cmWidth = 80;

    public final int titleHeight = 17;

    public final int maxCfgInLine = 5;

    public final int radius = 7;

    private float draggedFactor = 0;

    private final int windowHeight = 200;
    private final int windowWidth = 400;

    private ButtonArea buttonArea;

    private float configsDrawY = titleHeight + bigPadding;

    private float scrollVelocityY = 0f;
    private static final float SCROLL_FRICTION = 0.7f;
    private static final float SCROLL_SENSITIVITY = 5f;

    private float bounceBackFactor = 0f;

    private float delta = 1;
    private Path oldPath;
    private Path currentPath;

    public ConfigWindowArea() {
        super();
        Main.EVENT_BUS.subscribe(this);

        this.buttonArea = new ButtonArea.ButtonBuilder("open in explorer")
            .onClick(Managers.CONFIG::openFilesDir)
            .build();

        for (Path path : Managers.CONFIG.getFiles()) {
            areas.add(new ConfigArea(
                    this, path
            ));
        }

        Path cfg = Managers.CONFIG.getCurrent();
        oldPath = cfg;
        currentPath = cfg;
    }

    public ConfigArea getCfgArea(Path path) {
        for (RenderArea area : areas) {
            if (area instanceof ConfigArea configArea) {
                if (configArea.config.equals(path)) return configArea;
            }
        }
        return null;
    }

    public void resetCM() {
        if (cm != null) {
            cm = null;
        }
    }
    public void setCM(ContextMenu cm) {
        this.cm = cm;
    }

    public void updateConfigs() {
        List<Path> currentConfigs = Managers.CONFIG.getFiles();

        List<ConfigArea> newConfigAreas = new ArrayList<>();
        for (Path path : currentConfigs) {
            ConfigArea existing = getCfgArea(path);
            if (existing != null) {
                newConfigAreas.add(existing);
            } else {
                newConfigAreas.add(new ConfigArea(this, path));
            }
        }
        for (int i = areas.size() - 1; i >= 0; i--) {
            if (areas.get(i) instanceof ConfigArea) {
                areas.remove(i);
            }
        }
        areas.addAll(0, newConfigAreas);
    }

    @EventHandler
    private void onChangeConfig(OnChangeConfig e) {
        delta = 0;
        oldPath = e.getOld();
        currentPath = e.getCurrent();
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        height = windowHeight;
        width = windowWidth;

        updateConfigs();

        startY -= (int) (50 * (1 - showFactor));

        Render2D.fill(
            context, startX, startY,
            startX + windowWidth,
            startY + windowHeight,
            CategoryArea.makeAColor((150 + (30 * draggedFactor)) * showFactor),
            radius, 3
        );
        Render2D.fillPart(
            context, startX, startY,
            startX + windowWidth,
            startY + radius,
            ColorUtils.fromRGB(0, 0, 0, (50) * showFactor),
            3, true
        );
        context.fill(
            startX, startY + radius,
            startX + windowWidth, startY + radius + (titleHeight - radius),
            ColorUtils.fromRGB(0, 0, 0, 50 * showFactor)
        );
        String name = "config window";
        context.drawText(
            textRenderer,
            name,
            (startX + width - bigPadding) - textRenderer.getWidth(name),
            startY + ((titleHeight / 2) - (textRenderer.fontHeight / 2)),
            ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
            false
        );

        buttonArea.render(
            context, startX + radius, startY + ((titleHeight / 2) - (buttonArea.height / 2)),
            -1, -1, mouseX, mouseY
        );

        int totalPadding = bigPadding * (maxCfgInLine + 1);
        int availableWidth = width - totalPadding;
        int size = Math.max(0, availableWidth / maxCfgInLine);

        context.enableScissor(
            startX, startY + titleHeight, startX + width, startY + height - 1
        );

        ConfigArea oldArea = getCfgArea(oldPath);
        ConfigArea currentArea = getCfgArea(currentPath);
        if (oldArea == null || currentArea == null) {
            currentPath = Managers.CONFIG.getCurrent();
            oldPath = currentPath;
            currentArea = getCfgArea(currentPath);
            oldArea = currentArea;
        }
        if (oldArea != null && currentArea != null) {
            Render2D.fill(
                context,
                MathHelper.lerp(delta, oldArea.x, currentArea.x),
                MathHelper.lerp(delta, oldArea.y, currentArea.y),
                MathHelper.lerp(delta, oldArea.x + oldArea.width, currentArea.x + currentArea.width),
                MathHelper.lerp(delta, oldArea.y + oldArea.height, currentArea.y + currentArea.height),
                ColorUtils.fromRGB(0, 0, 0, 30 * showFactor),
                bigPadding, 2
            );
        }

        int configsX = startX + bigPadding;
        int configsY = (int) (startY + configsDrawY);
        for (RenderArea area : areas) {
            if ((configsX - startX) + area.width + bigPadding > width) {
                configsY += area.height + bigPadding;
                configsX = startX + bigPadding;
            }
            area.render(context, configsX, configsY, size, size, mouseX, mouseY);
            configsX += area.width + bigPadding;
        }
        context.disableScissor();

        if (cm != null) {
            cm.render(context, -1, -1, cmWidth, -1, mouseX, mouseY);
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (checkHovered(mouseX, mouseY)) {
            scrollVelocityY += (float) scrollY * SCROLL_SENSITIVITY;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (buttonArea.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (cm != null && cm.mouseClicked(mouseX, mouseY, button)) return true;
        if (buttonArea.mouseClicked(mouseX, mouseY, button)) return true;
        for (RenderArea a : areas) {
            if (a.mouseClicked(mouseX, mouseY, button)) return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && checkHovered(mouseX, mouseY)) {
            int x = (int) mouseX;
            int y = (int) mouseY;
            setCM(
                new ContextMenu.CMBuilder()
                    .position(new int[]{x, y})
                    .parentArea(this)
                    .areas(new ButtonArea[]{
                        new ButtonArea.ButtonBuilder("+ create")
                            .onClick(Managers.CONFIG::createFile)
                            .build()
                    })
                    .build()
            );

            return true;
        }
        return false;
    }

    private float calculateTotalContentHeight() {
        int numConfigs = Managers.CONFIG.getFiles().size();
        if (numConfigs == 0) return 0;

        int rows = (numConfigs + maxCfgInLine - 1) / maxCfgInLine;
        int size = (windowWidth - (bigPadding * (maxCfgInLine + 1))) / maxCfgInLine;
        return rows * (size + bigPadding) - bigPadding;
    }

    public void changeConfigsY(float factor) {
        float totalHeight = calculateTotalContentHeight();
        float availableHeight = windowHeight - titleHeight - 2 * bigPadding;
        float maxScroll = titleHeight + bigPadding;
        float minScroll = maxScroll - Math.max(0, totalHeight - availableHeight);

        configsDrawY = maxScroll - factor * (maxScroll - minScroll);
        configsDrawY = MathHelper.clamp(configsDrawY, minScroll, maxScroll);

        bounceBackFactor = 0;
        scrollVelocityY = 0;
    }

    @Override
    public void animHandler() {
        if (mc.currentScreen instanceof ConfigsGui configsGui) {
            draggedFactor = AnimHelper.handle(configsGui.dragged, draggedFactor);
        }
        showFactor = AnimHelper.handle(show, showFactor, AnimHelper.AnimMode.EaseOut);

        configsDrawY += scrollVelocityY;
        scrollVelocityY *= SCROLL_FRICTION;
        if (Math.abs(scrollVelocityY) < 0.1f) {
            scrollVelocityY = 0f;
        }
        float totalHeight = calculateTotalContentHeight();
        float availableHeight = windowHeight - titleHeight - 2 * bigPadding;
        float maxScroll = titleHeight + bigPadding;
        float minScroll = maxScroll - Math.max(0, totalHeight - availableHeight);

        if (configsDrawY > maxScroll) {
            float overshoot = configsDrawY - maxScroll;
            bounceBackFactor = AnimHelper.handle(true, bounceBackFactor);
            configsDrawY = maxScroll + overshoot * (1 - bounceBackFactor);
        }
        else if (configsDrawY < minScroll) {
            float overshoot = minScroll - configsDrawY;
            bounceBackFactor = AnimHelper.handle(true, bounceBackFactor);
            configsDrawY = minScroll - overshoot * (1 - bounceBackFactor);
        }
        else {
            bounceBackFactor = 0;
        }

        delta = AnimHelper.handle(true, delta);
    }

    public class ConfigArea extends RenderArea {
        private Path config;
        private ConfigWindowArea parentArea;

        private boolean inputting = false;

        private String inputText = "";

        private boolean inputLight = false;
        private float inputLightFactor = 0;

        public ConfigArea(ConfigWindowArea parentArea, Path config) {
            super();
            this.parentArea = parentArea;
            this.config = config;
        }

        @Override
        public void render(
            DrawContext context,
            int startX, int startY,
            int width, int height,
            double mouseX, double mouseY
        ) {
            TextRenderer textRenderer = mc.textRenderer;

            Render2D.fill(
                context,
                startX,
                startY,
                startX + width,
                startY + height,
                ColorUtils.fromRGB(0, 0, 0, 40 * showFactor),
                bigPadding, 2
            );
            String name = inputting ? (inputText.isEmpty() ? "..." : inputText) + (inputLightFactor > 0.5f ? "|" : "") : getName(config);

            int nameHeight = textRenderer.fontHeight + (padding * 2);
            int nameWidth = width - (padding * 2);
            int nameX = startX + padding;
            int nameY = startY + height - nameHeight - padding;
            Render2D.fill(
                context,
                nameX, nameY,
                nameX + nameWidth,
                nameY + nameHeight,
                ColorUtils.fromRGB(0, 0, 0, 50 * showFactor),
                bigPadding, 2
            );
            context.enableScissor(
                nameX + 1, nameY,
                nameX + nameWidth - 1,
                nameY + nameHeight
            );
            context.drawText(
                textRenderer, name,
                nameX + ((nameWidth / 2) - (textRenderer.getWidth(name) / 2)),
                nameY + padding,
                ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
                false
            );
            context.disableScissor();

            super.render(context, startX, startY, width, height, mouseX, mouseY);
        }

        public String getName(Path path) {
            String fileName = path.getFileName().toString();
            int lastDotIndex = fileName.lastIndexOf('.');
            return lastDotIndex > 0
                    ? fileName.substring(0, lastDotIndex)
                    : fileName;
        }

        @Override
        public void animHandler() {
            showFactor = parentArea.showFactor;
            if (inputting) {
                if (inputLightFactor == 1) {
                    inputLight = false;
                } else if (inputLightFactor == 0) {
                    inputLight = true;
                }
            } else {
                inputLight = false;
            }
            inputLightFactor = AnimHelper.handle(inputLight, inputLightFactor);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int nameHeight = mc.textRenderer.fontHeight + (padding * 3);
            if (checkHovered(x, y + height - nameHeight, width, nameHeight, mouseX, mouseY)) {
                inputText = getName(config);
                inputting = true;
                return true;
            } else {
                inputting = false;
            }
            boolean hovered = checkHovered(mouseX, mouseY);
            if (hovered && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                int x = (int) mouseX;
                int y = (int) mouseY;
                setCM(
                    new ContextMenu.CMBuilder()
                        .position(new int[]{x, y})
                        .parentArea(parentArea)
                        .areas(new ButtonArea[]{
                            new ButtonArea.ButtonBuilder("✏ rename")
                                .onClick(() -> {
                                    inputting = true;
                                    inputText = getName(config);
                                })
                                .build(),
                            new ButtonArea.ButtonBuilder("✔ set current")
                                .onClick(() -> {
                                    Managers.CONFIG.setCurrent(config);
                                })
                                .build(),
                            new ButtonArea.ButtonBuilder("❌ delete")
                                .onClick(() -> {
                                    Managers.CONFIG.deleteFile(config);
                                })
                                .color(ColorUtils.fromRGB(255, 230, 230))
                                .build(),
                        })
                        .build()
                );

                return true;
            } else if (hovered) {
                Managers.CONFIG.setCurrent(config);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (inputting) {
                inputText += chr;
                return true;
            }
            return super.charTyped(chr, modifiers);
        }
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (inputting) {
                if (keyCode == GLFW.GLFW_KEY_V && modifiers != 0) {
                    inputText += mc.keyboard.getClipboard();
                } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputText.isEmpty()) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                } else if (Main.cancelButtons.contains(keyCode)) {
                    inputting = false;
                } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    inputting = false;
                    Managers.CONFIG.renameFile(config, inputText);
                }
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
