package com.skylong.gui;

import com.skylong.config.ConfigManager;
import com.skylong.modules.settings.*;
import com.skylong.utils.GetColor;
import com.skylong.modules.Parent;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGuiScreen extends Screen {
    private ConfigManager config = ConfigManager.getInstance("click_gui");

    private final Map<String, List<Parent>> modules;

    public static final String hintsText = "← ↑ ↓ → - move gui\nleft shift - percent binds\nmouse middle - bind module";

    public Parent bindingModule = null;
    public int bindAnimPercent = 0;
    public boolean bindAnimReverse = true;

    public int animPercent = 0;
    public boolean animReverse = false;

    public int showKeybind = 0;

    public double lastMouseX = 0;
    public double lastMouseY = 0;

    public int xMove = 0;
    public int yMove = 0;
    public static int moveDifference = 10;

    private final Screen previous;

    public Map<Parent, Object> hoverAnimations = new HashMap<>();

    public Map<Parent, Object> setAnimations = new HashMap<>();
    public Map<Parent, Object> setAnimReverse = new HashMap<>();

    public List<ModuleArea> moduleAreas = new ArrayList<>();

    public ClickGuiScreen(Screen previous, Map<String, List<Parent>> modules) {
        super(Text.literal("ClickGui"));
        this.previous = previous;
        this.modules = modules;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        if (previous != null) {
            previous.render(context, mouseX, mouseY, delta);
        }
        animHandler(client);
        if (animReverse && animPercent == 0) {
            client.setScreen(null);
            return;
        }

        // Фон с градиентом
        int alphaTop = 200 * animPercent / 100;
        int alphaBottom = 50 * animPercent / 100;
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 1);
        context.getMatrices().scale(1, 1, 1);
        context.fillGradient(0, 0, width, height,
                GetColor.getColor(0, 0, 0, alphaTop),
                GetColor.getColor(0, 0, 0, alphaBottom)
        );
        context.getMatrices().pop();
        // подсказки
        float hintsScale = 0.7f;
        int alpha = 150 * animPercent / 100;
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

        moduleAreas.clear();

        // параметры
        int yStart = (10 + yMove) * animPercent / 100;
        int baseTextHeight = textRenderer.fontHeight;
        int spacing = 10;
        int spacingColumns = 10;
        int numCols = modules.size();
        int columnWidth = Math.min(70, (width - spacingColumns * (numCols - 1)) / numCols);
        int totalColsWidth = numCols * columnWidth + (numCols - 1) * spacingColumns;
        float xColStart = (float) ((width + xMove) - totalColsWidth) / 2;

        for (Map.Entry<String, List<Parent>> entry : modules.entrySet()) {
            String category = entry.getKey();
            List<Parent> list = entry.getValue();

            // Название категории
            float categoryScale = 1.2f;
            context.getMatrices().push();
            context.getMatrices().translate(xColStart, yStart, 2);
            context.getMatrices().scale(categoryScale, categoryScale, 2);
            context.drawTextWithShadow(
                textRenderer,
                Text.literal(category).formatted(Formatting.BOLD),
                0,
                0,
                GetColor.getColor(255, 255, 255, 255 * animPercent / 100)
            );
            context.getMatrices().pop();

            // Начинаем аккумулировать y для модулей под категорией
            float yOffset = yStart + (baseTextHeight + spacing);

            for (Parent module : list) {
                boolean hovered = (
                    (
                        mouseX >= xColStart
                        && mouseX <= xColStart + columnWidth
                        && mouseY >= yOffset
                        && mouseY <= yOffset + baseTextHeight
                    ) || (
                        setAnimations.get(module) != null
                    )
                );

                int hoverPercent = (int) hoverAnimations.getOrDefault(module, 0);
                double t = hoverPercent / 100.0;
                // крутая ease-in-out анимация
                int maxStep = 10;
                int easeDelta = (int) Math.ceil(maxStep * (hovered ? (1 - t) : t));
                easeDelta = Math.max(easeDelta, 1);
                if (hovered) {
                    hoverPercent = Math.min(hoverPercent + easeDelta, 100);
                } else {
                    hoverPercent = Math.max(hoverPercent - easeDelta, 0);
                }
                hoverAnimations.put(module, hoverPercent);

                // Коэффициент масштабирования: от 1.0 до 1.2 (можно подкорректировать)
                float maxScaleDelta = 0.2f;
                float scale = 1.0f + maxScaleDelta * (hoverPercent / 100f);
                float scaledHeight = baseTextHeight * scale;

                // Готовим текст
                int keyBind = module.getKeybind();
                String moduleName = module.getName();
                String name = moduleName;  // по умолчанию — имя модуля
                if (showKeybind > 1) {
                    if (keyBind != -1) {
                        String key = keyName(keyBind);
                        name = getAnimText(moduleName, key, showKeybind);
                    }
                } else if (bindAnimPercent > 1 && bindingModule == module) {
                    name = getAnimText(moduleName, "...", bindAnimPercent);
                }
                Formatting fmt = module.getEnable()
                        ? Formatting.UNDERLINE
                        : Formatting.RESET;
                Text display = Text.literal(name).formatted(fmt);

                // Цвет текста
                int baseAlpha = 255 * animPercent / 100;
                int color = GetColor.getColor(255, 255, 255, baseAlpha);

                List<Setting<?>> sets = module.getSettings();
                float winHeight = 0;
                float xDifference = 0;
                if (setAnimations.containsKey(module) && !module.getSettings().isEmpty()) {
                    winHeight = drawSettings(module, sets, context, yOffset, xColStart, columnWidth, scale, baseTextHeight);
                } else if (module.getSettings().isEmpty() && setAnimations.containsKey(module)) {
                    setAnimReverse.put(module, true);
                    // эта переменная может быть от 15
                    int percent = (int) setAnimations.get(module);

                    if (percent % 2 == 0) {
                        xDifference -= 5f;
                    } else {
                        xDifference += 5f;
                    }
                    xDifference = xDifference * percent / 100;
                }

                // Рисуем с масштабомx
                context.getMatrices().push();
                context.getMatrices().translate(xColStart + xDifference, yOffset, 4);
                context.getMatrices().scale(scale, scale, 4);
                context.drawTextWithShadow(textRenderer, display, 0, 0, color);
                context.getMatrices().pop();

                // Запоминаем область для клика
                moduleAreas.add(new ModuleArea(
                    module,
                    xColStart,
                    yOffset,
                    (textRenderer.getWidth(name) * scale),
                    scaledHeight
                ));

                // Сдвигаем yOffset на высоту отрисованного текста + отступ
                yOffset += scaledHeight + spacing + winHeight;
            }

            xColStart += columnWidth + spacingColumns;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // еее навалим говно кода, погнали
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && animPercent >= 100 && !animReverse) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj != null) {
                if (obj instanceof Parent module) {
                    module.setEnable(!module.getEnable());
                    return true;
                } else if (obj instanceof ListSetting set) {
                    int i = set.getOptions().indexOf(set.getValue());
                    i += 1;
                    if (i > set.getOptions().size() - 1) {
                        i = 0;
                    }
                    set.setValue(set.getOptions().get(i));
                } else if (obj instanceof Setting set) {
                    if (set.getValue() instanceof Boolean) {
                        set.setValue(! (Boolean) set.getValue());
                    } else if (set.getValue() instanceof Float) {
                        float value = (float) set.getValue() + 0.1f;
                        value = Math.min(Math.max(value, (float) set.min), (float) set.max);
                        value = Math.round(value * 10f) / 10f;
                        set.setValue(value);
                    } else if (set.getValue() instanceof Integer) {
                        int value = (int) set.getValue() + 1;
                        set.setValue(
                            Math.clamp(value, (int) set.min, (int) set.max)
                        );
                    }
                }
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && animPercent >= 100 && !animReverse) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj != null) {
                if (obj instanceof Parent module) {
                    if (!setAnimations.containsKey(module)) {
                        setAnimations.put(module, 0);
                        setAnimReverse.put(module, false);
                    } else {
                        setAnimReverse.put(module, true);
                    }
                    return true;
                } else if (obj instanceof ListSetting set) {
                    int i = set.getOptions().indexOf(set.getValue());
                    i -= 1;
                    if (i < 0) {
                        i = set.getOptions().size() - 1;
                    }
                    set.setValue(set.getOptions().get(i));
                } else if (obj instanceof Setting set) {
                    if (set.getValue() instanceof Boolean) {
                        set.setValue(! (Boolean) set.getValue());
                    } else if (set.getValue() instanceof Float) {
                        float value = (float) set.getValue() - 0.1f;
                        value = Math.min(Math.max(value, (float) set.min), (float) set.max);
                        value = Math.round(value * 10f) / 10f;
                        set.setValue(value);
                    } else if (set.getValue() instanceof Integer) {
                        int value = (int) set.getValue() - 1;
                        set.setValue(
                            Math.clamp(value, (int) set.min, (int) set.max)
                        );
                    }
                }
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && animPercent >= 100 && !animReverse) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj != null) {
                if (obj instanceof Parent module) {
                    bindingModule = module;
                    bindAnimReverse = false;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            int value = (keyCode == GLFW.GLFW_KEY_ESCAPE) ? -1 : keyCode;
            bindingModule.setKeybind(value);
            bindAnimReverse = true;
            return true;
        }
        if (
            keyCode == GLFW.GLFW_KEY_RIGHT ||
            keyCode == GLFW.GLFW_KEY_LEFT ||
            keyCode == GLFW.GLFW_KEY_DOWN ||
            keyCode == GLFW.GLFW_KEY_UP
        ) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                yMove -= moveDifference;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                yMove += moveDifference;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                xMove += moveDifference;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                xMove -= moveDifference;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animReverse = true;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (client != null && client.player != null && animPercent >= 100 && !animReverse && (boolean) config.get("mouse_move")) {
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

    public float drawSettings(
        Parent module,
        List<Setting<?>> sets,
        DrawContext context,
        float yOffset,
        float xColStart,
        int columnWidth,
        float scale,
        int baseTextHeight
    ) {
        // параметры
        int zDepth = 3;
        int setAnimPercent = (int) setAnimations.get(module);
        int alphaColor = (255 * animPercent / 100) * setAnimPercent / 100;
        int paddingBelowText = 5;
        int rectY = (int) (yOffset + baseTextHeight * scale + paddingBelowText);
        int spacing = 5;

        float ySetOffset = paddingBelowText + rectY;

        for (Setting set : sets) {
            if (
                set.getClass() == Setting.class ||
                set.getClass() == TextSetting.class ||
                set.getClass() == ListSetting.class
            ) {
                int color = GetColor.getColor(255, 255, 255, alphaColor);
                String name;

                if (set.getClass() == ListSetting.class) {
                    name = set.getName() + ": " + set.getValue();
                } else if (set.getValue() != null) {
                    name = set.getName() + ": " + set.getValue();

                    if (set.getValue().getClass() == Boolean.class) {
                        name = set.getName() + ": " + ((boolean) set.getValue() ? "1" : "0");
                        if ((boolean) set.getValue()) {
                            color = GetColor.getColor(230, 255, 230, alphaColor);
                        } else {
                            color = GetColor.getColor(255, 230, 230, alphaColor);
                        }
                    }

                } else {
                    name = set.getName();
                    color = GetColor.getColor(200, 200, 200, alphaColor);
                }

                float textScale = 0.8f;
                float drawOffsetY = 10 * (setAnimPercent - 100) / 100f;
                float drawX = xColStart;
                float drawY = ySetOffset + drawOffsetY;
                float width = textRenderer.getWidth(name) * textScale;
                float height = textRenderer.fontHeight * textScale;
                Text display = Text.literal(name);

                context.getMatrices().push();
                context.getMatrices().translate(drawX, drawY, zDepth);
                context.getMatrices().scale(textScale, textScale, zDepth);
                context.drawTextWithShadow(textRenderer, display, 0, 0, color);
                context.getMatrices().pop();

                moduleAreas.add(new ModuleArea(
                    set,
                    drawX,
                    drawY,
                    width,
                    height
                ));

                ySetOffset += (textRenderer.fontHeight * textScale) + spacing;
            }
        }
        ySetOffset = (ySetOffset - rectY) * setAnimPercent / 100;

        // можно вернуть если захочется

//        context.getMatrices().push();
//        context.getMatrices().translate(0, 0, zDepth);
//        context.fill(
//            (int) xColStart,
//            rectY,
//            (int) (xColStart + columnWidth),
//            rectY + ySetOffset,
//            GetColor.getColor(0, 0, 0, alphaWindow)
//        );
//        context.getMatrices().pop();

        return ySetOffset;
    }

    public static String keyName(int keyKode) {
        InputUtil.Key key = InputUtil.fromKeyCode(keyKode, GLFW.glfwGetKeyScancode(keyKode));
        String keyName = key.getTranslationKey()
                .substring(key.getTranslationKey().lastIndexOf('.') + 1)
                .toUpperCase();
        return keyName;
    }

    private String getAnimText(String startText, String endText, int percent) {
        percent = Math.max(0, Math.min(100, percent));
        float pct = percent / 100f;
        if (pct <= 0.5f) {
            float keepRatio = 1 - pct * 2;
            int keepChars = Math.round(startText.length() * keepRatio);
            return startText.substring(0, keepChars);
        } else {
            float drawRatio = (pct - 0.5f) * 2;
            int drawChars = Math.round(endText.length() * drawRatio);
            return endText.substring(0, drawChars);
        }
    }

    private Object getModuleUnderMouse(int mouseX, int mouseY) {
        for (ModuleArea area : moduleAreas) {
            if (
                mouseX >= area.x
                && mouseX <= area.x + area.width
                && mouseY >= area.y
                && mouseY <= area.y + area.height
            ) {
                return area.module;
            }
        }
        return null;
    }

    public void animHandler(MinecraftClient client) {
        if (animPercent > 99) {
            GLFW.glfwSetScrollCallback(client.getWindow().getHandle(), (window, xoffset, yoffset) -> {
                if (client.currentScreen == this) {
                    xMove += xoffset * 4;
                    yMove -= yoffset * 4;
                }
            });
        }

        // Анимация открытия/закрытия
        if (!animReverse && animPercent < 100) {
            animPercent += Math.max(1, (15 * (100 - animPercent)) / 100);
        } else if (animReverse && animPercent > 0) {
            animPercent -= Math.max(1, (15 * animPercent) / 100);
        }
        animPercent = Math.clamp(animPercent, 0, 100);

        // анимация биндов
        long window = client.getWindow().getHandle();
        if (!animReverse && animPercent >= 100) {
            boolean shiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
            if (shiftDown && showKeybind < 100) {
                showKeybind += Math.max(1, (15 * (100 - showKeybind)) / 100);
            } else if (!shiftDown && showKeybind > 0) {
                showKeybind -= Math.max(1, (15 * showKeybind) / 100);
            }
        }
        else if (animReverse && showKeybind > 0) {
            showKeybind -= Math.max(1, (15 * showKeybind) / 100);
        }
        showKeybind = Math.clamp(showKeybind, 0, 100);

        // анимация бинда
        if (!bindAnimReverse && bindAnimPercent < 100) {
            bindAnimPercent += Math.max(1, (15 * (100 - bindAnimPercent)) / 100);
        } else if (bindAnimReverse && bindAnimPercent > 0) {
            bindAnimPercent -= Math.max(1, (15 * bindAnimPercent) / 100);
        }
        bindAnimPercent = Math.clamp(bindAnimPercent, 0, 100);
        if (bindAnimReverse && bindAnimPercent < 1) {
            bindingModule = null;
        }

        // анимация "правого клика" (setAnimations)
        if (!setAnimations.isEmpty()) {
            Iterator<Parent> it = setAnimations.keySet().iterator();
            while (it.hasNext()) {
                Parent module = it.next();
                int percent = (int) setAnimations.get(module);
                boolean reverse = (boolean) setAnimReverse.getOrDefault(module, false);

                if (!reverse && percent < 100) {
                    percent += Math.max(1, (15 * (100 - percent)) / 100);
                } else if (reverse && percent > 0) {
                    percent -= Math.max(1, (15 * percent) / 100);
                }

                percent = Math.clamp(percent, 0, 100);
                setAnimations.put(module, percent);

                if (reverse && percent == 0) {
                    it.remove();
                    setAnimReverse.remove(module);
                }
            }
        }
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