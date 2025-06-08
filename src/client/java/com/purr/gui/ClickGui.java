// местая зона отчуждения, просьба не заходить без подготовки (хотя бы моральной)
package com.purr.gui;

import com.purr.modules.ModuleManager;
import com.purr.modules.ui.Gui;
import com.purr.modules.settings.*;
import com.purr.utils.RGB;
import com.purr.utils.GetAnimDiff;
import com.purr.modules.Parent;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;

import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGui extends Screen {
    private final Map<String, List<Parent>> modules;

    public static final String hintsText = "← ↑ ↓ → - move gui\nleft shift - percent binds\nmouse middle - bind module";

    public Parent bindingModule = null;
    public float bindAnimPercent = 0;
    public boolean bindAnimReverse = true;

    public float animPercent = 0;
    public boolean animReverse = false;

    public int showKeybind = 0;

    public double lastMouseX = 0;
    public double lastMouseY = 0;

    public float xMove = 0;
    public float yMove = 0;

    private final Screen previous;

    public Map<Parent, Object> hoverAnimations = new HashMap<>();

    public Map<Parent, Object> setAnimations = new HashMap<>();
    public Map<Parent, Object> setAnimReverse = new HashMap<>();

    public Map<ListSetting, Float> exsAnim = new HashMap<>();
    public Map<ListSetting, Boolean> exsAnimReverse = new HashMap<>();

    public List<Object> moduleAreas = new ArrayList<>();

    private Gui guiModule;

    public ClickGui(Screen previous, ModuleManager moduleManager, Gui guiModule) {
        super(Text.literal("Purr Gui"));
        this.previous = previous;
        this.modules = moduleManager.getModules();
        this.guiModule = guiModule;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (previous != null) {
            previous.render(context, mouseX, mouseY, delta);
        }

        if (animReverse && animPercent == 0) {
            client.setScreen(null);
            return;
        }

        animHandler();

        float screenHeight = context.getScaledWindowHeight();
        float screenWidth = context.getScaledWindowWidth();

        if (animPercent == 100) {
            boolean left = GLFW.glfwGetMouseButton(
                    client.getInstance().getWindow().getHandle(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == GLFW.GLFW_PRESS;
            if (left) {
                Object obj = getModuleUnderMouse(mouseX, mouseY);
                if (obj != null && obj instanceof Setting set) {
                    if (set.getValue() instanceof Float) {;
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
            boolean right = GLFW.glfwGetMouseButton(
                    client.getInstance().getWindow().getHandle(),
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT
            ) == GLFW.GLFW_PRESS;
            if (right) {
                Object obj = getModuleUnderMouse(mouseX, mouseY);
                if (obj != null && obj instanceof Setting set) {
                    if (set.getValue() instanceof Float) {
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

        if (
            guiModule != null &&
            guiModule.image.getValue() != null &&
            !guiModule.image.getValue().equals("none")
        ) {
            String path = guiModule.getImages().get(guiModule.image.getValue());
            Identifier texture = Identifier.of("purr", path);
            try {
                Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(texture);
                NativeImage nativeImage = NativeImage.read(resource.get().getInputStream());

                float imageWidth = nativeImage.getWidth();
                float imageHeight = nativeImage.getHeight();

                nativeImage.close();

                float screenMin = Math.min(screenWidth, screenHeight);
                float fixedSize = screenMin * 0.5f;

                float scale = Math.min(fixedSize / imageWidth, fixedSize / imageHeight);

                float scaledWidth = imageWidth * scale;
                float scaledHeight = imageHeight * scale;

                float x = screenWidth - (scaledWidth * animPercent / 100);
                float y = (screenHeight - (scaledHeight * animPercent / 100)) + 1;

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 2);
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    texture,
                    (int) x,
                    (int) y,
                    0, 0,
                    (int) scaledWidth,
                    (int) scaledHeight,
                    (int) scaledWidth,
                    (int) scaledHeight
                );
                context.getMatrices().pop();
            } catch (Exception ignored) {}
        }

        // Фон с градиентом
        int alphaTop = 200 * (int) animPercent / 100;
        int alphaBottom = 50 * (int) animPercent / 100;
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 1);
        context.getMatrices().scale(1, 1, 1);
        context.fillGradient(0, 0, width, height,
            RGB.getColor(0, 0, 0, alphaTop),
            RGB.getColor(0, 0, 0, alphaBottom)
        );
        context.getMatrices().pop();

        // подсказки
        String[] lines = hintsText.split("\n");
        float hintsScale = 0.7f;
        int alpha = 150 * (int) animPercent / 100;
        int colorHints = RGB.getColor(255, 255, 255, alpha);
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

        moduleAreas.clear();

        // параметры
        float yStart = (10 + yMove) * animPercent / 100;
        int baseTextHeight = textRenderer.fontHeight;
        int spacing = 10;
        int spacingColumns = 10;
        int numCols = modules.size();
        int columnWidth = Math.min(70, (width - spacingColumns * (numCols - 1)) / numCols);
        int totalColsWidth = numCols * columnWidth + (numCols - 1) * spacingColumns;
        float xColStart = ((width + xMove) - totalColsWidth) / 2;

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
                RGB.getColor(255, 255, 255, 255 * (int) animPercent / 100)
            );
            context.getMatrices().pop();

            // Начинаем аккумулировать y для модулей под категорией
            float yOffset = yStart + (baseTextHeight + spacing);

            for (Parent module : list) {
                if (!module.getVisible()) continue;
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
                int easeDelta = (int) Math.ceil(GetAnimDiff.get() * (hovered ? (1 - t) : t));
                easeDelta = Math.max(easeDelta, 1);
                if (hovered) {
                    hoverPercent = Math.min(hoverPercent + easeDelta, 100);
                } else {
                    hoverPercent = Math.max(hoverPercent - easeDelta, 0);
                }
                hoverAnimations.put(module, hoverPercent);

                float maxScaleDelta = 0.2f;
                float scale = 1.0f + maxScaleDelta * (hoverPercent / 100f);
                float scaledHeight = baseTextHeight * scale;

                // Готовим текст
                int keyBind = module.getKeybind();
                String moduleName = module.getName();
                String name = moduleName;
                if (showKeybind > 1) {
                    if (keyBind != -1) {
                        String key = keyName(keyBind);
                        name = getAnimText(moduleName, key, showKeybind);
                    }
                } else if (bindAnimPercent > 1 && bindingModule == module) {
                    name = getAnimText(moduleName, "...", (int) bindAnimPercent);
                }
                Formatting fmt = module.getEnable()
                        ? Formatting.UNDERLINE
                        : Formatting.RESET;
                Text display = Text.literal(name).formatted(fmt);

                // Цвет текста
                int baseAlpha = 255 * (int) animPercent / 100;
                int color = RGB.getColor(255, 255, 255, baseAlpha);

                List<Setting<?>> sets = module.getSettings();
                float winHeight = 0;
                float xDifference = 0;
                if (setAnimations.containsKey(module) && !module.getSettings().isEmpty()) {
                    winHeight = drawSettings(module, sets, context, yOffset, xColStart, scale, baseTextHeight);
                } else if (module.getSettings().isEmpty() && setAnimations.containsKey(module)) {
                    setAnimReverse.put(module, true);
                    // эта переменная может быть от 15
                    float percent = (float) setAnimations.get(module);

                    if (percent % 2 == 0) {
                        xDifference -= 5f;
                    } else {
                        xDifference += 5f;
                    }
                    xDifference = xDifference * percent / 100;
                }

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
            if (obj instanceof ListArea lst) {
                lst.set();
                return true;
            } else if (obj instanceof Group group) {
                group.setOpen(!group.isOpen());
                return true;
            } else if (obj instanceof ListSetting set) {
                int i = set.getOptions().indexOf(set.getValue());
                i += 1;
                if (i > set.getOptions().size() - 1) {
                    i = 0;
                }
                set.setValue(set.getOptions().get(i));
                return true;
            } else if (obj instanceof Setting set) {
                if (set.getValue() instanceof Boolean) {
                    set.setValue(! (Boolean) set.getValue());
                }
                return true;
            } else if (obj instanceof Parent module) {
                module.setEnable(!module.getEnable());
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && animPercent >= 100 && !animReverse) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof ListSetting set) {
                if (!exsAnim.containsKey(set)) {
                    exsAnim.put(set, 0.0F);
                    exsAnimReverse.put(set, false);
                } else {
                    exsAnimReverse.put(set, true);
                }
                return true;
            } else if (obj instanceof Group group) {
                group.setOpen(!group.isOpen());
                return true;
            } else if (obj instanceof Setting set) {
                if (set.getValue() instanceof Boolean) {
                    set.setValue(! (Boolean) set.getValue());
                }
                return true;
            } else if (obj instanceof Parent module) {
                if (!setAnimations.containsKey(module)) {
                    setAnimations.put(module, 0.0F);
                    setAnimReverse.put(module, false);
                } else {
                    setAnimReverse.put(module, true);
                }
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && animPercent >= 100 && !animReverse) {
            Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
            if (obj instanceof Parent module) {
                bindingModule = module;
                bindAnimReverse = false;
                return true;
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
                yMove -= 10;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                yMove += 10;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                xMove += 10;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                xMove -= 10;
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
        if (client != null && client.player != null && animPercent >= 100 && !animReverse && guiModule.mouseMove.getValue()) {
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

    public void animHandler() {
        int animDiff = GetAnimDiff.get();

        // Анимация открытия/закрытия
        if (!animReverse && animPercent < 100) {
            animPercent += Math.max(0.1f, (animDiff * (100 - animPercent)) / 100);
        } else if (animReverse && animPercent > 0) {
            animPercent -= Math.max(0.1f, (animDiff * animPercent) / 100);
        }
        animPercent = Math.clamp(animPercent, 0, 100);

        // анимация биндов
        long window = client.getWindow().getHandle();
        if (!animReverse && animPercent >= 100) {
            boolean shiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
            if (shiftDown && showKeybind < 100) {
                showKeybind += Math.max(0.1f, (animDiff * (100 - showKeybind)) / 100);
            } else if (!shiftDown && showKeybind > 0) {
                showKeybind -= Math.max(0.1f, (animDiff * showKeybind) / 100);
            }
        }
        else if (animReverse && showKeybind > 0) {
            showKeybind -= Math.max(0.1f, (animDiff * showKeybind) / 100);
        }
        showKeybind = Math.clamp(showKeybind, 0, 100);

        // анимация бинда
        if (!bindAnimReverse && bindAnimPercent < 100) {
            bindAnimPercent += Math.max(0.1f, (animDiff * (100 - bindAnimPercent)) / 100);
        } else if (bindAnimReverse && bindAnimPercent > 0) {
            bindAnimPercent -= Math.max(0.1f, (animDiff * bindAnimPercent) / 100);
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
                float percent = (float) setAnimations.get(module);
                boolean reverse = (boolean) setAnimReverse.getOrDefault(module, false);

                if (!reverse && percent < 100) {
                    percent += Math.max(0.1f, (animDiff * (100 - percent)) / 100);
                } else if (reverse && percent > 0) {
                    percent -= Math.max(0.1f, (animDiff * percent) / 100);
                }

                percent = Math.clamp(percent, 0, 100);
                setAnimations.put(module, percent);

                if (reverse && percent <= 1) {
                    it.remove();
                    setAnimReverse.remove(module);
                }
            }
        }
        // анимация открытия списка
        if (!exsAnim.isEmpty()) {
            Iterator<ListSetting> it = exsAnim.keySet().iterator();
            while (it.hasNext()) {
                ListSetting module = it.next();
                float percent = exsAnim.get(module);
                boolean reverse = exsAnimReverse.getOrDefault(module, false);

                if (!reverse && percent < 100) {
                    percent += Math.max(0.1f, (animDiff * (100 - percent)) / 100);
                } else if (reverse && percent > 0) {
                    percent -= Math.max(0.1f, (animDiff * percent) / 100);
                }

                percent = Math.clamp(percent, 0, 100);
                exsAnim.put(module, percent);

                if (reverse && percent <= 1) {
                    it.remove();
                    exsAnimReverse.remove(module);
                }
            }
        }
    }

    public float drawSettings(
        Parent module,
        List<Setting<?>> sets,
        DrawContext context,
        float yOffset,
        float xColStart,
        float scale,
        int baseTextHeight
    ) {
        // параметры
        int zDepth = 3;
        float setAnimPercent = (float) setAnimations.get(module);
        int alphaColor = (255 * (int) animPercent / 100) * (int) setAnimPercent / 100;
        int paddingBelowText = 5;
        int rectY = (int) (yOffset + baseTextHeight * scale + paddingBelowText);
        int spacing = 5;

        float ySetOffset = paddingBelowText + rectY;

        Group currentGroup = null;
        Group lastDrawGroup = null;

        for (Setting set : sets) {
            float textScale = 0.8f;
            int color = RGB.getColor(255, 255, 255, alphaColor);
            String name;
            currentGroup = set.getGroup();

            if (currentGroup != null && lastDrawGroup != currentGroup) {
                Text hearderText = Text.literal(currentGroup.getName() + (currentGroup.isOpen() ? " -" : " +"));
                context.getMatrices().push();
                context.getMatrices().translate(xColStart, ySetOffset + (10 * (setAnimPercent - 100) / 100f), zDepth);
                context.getMatrices().scale(textScale, textScale, zDepth);
                int colorE = (int) (200 * setAnimPercent / 100);
                context.drawTextWithShadow(
                    textRenderer, hearderText, 0, 0,
                    RGB.getColor(colorE, colorE, colorE, alphaColor)
                );
                context.getMatrices().pop();

                moduleAreas.add(new ModuleArea(
                    currentGroup,
                    xColStart,
                    ySetOffset,
                    textRenderer.getWidth(hearderText),
                    textRenderer.fontHeight
                ));

                ySetOffset += (textRenderer.fontHeight * textScale) + spacing;
                lastDrawGroup = currentGroup;
            }
            if (currentGroup != null && !currentGroup.isOpen()) {
                continue;
            }

            if (set instanceof ListSetting lst) {
                if (exsAnim.get(lst) != null && exsAnim.get(lst) != 0.0f) {
                    float exsPercent = setAnimPercent * exsAnim.getOrDefault(lst, 0.0f) / 100;
                    float drawOffsetY = 10 * (setAnimPercent - 100) / 100f;
                    float drawX = xColStart + spacing;
                    float headerY = ySetOffset + drawOffsetY;
                    Text hearderText = Text.literal(set.getName() + ": " + getAnimText((String) lst.getValue(), "", (int) exsPercent));
                    context.getMatrices().push();
                    context.getMatrices().translate(drawX - spacing, headerY, zDepth);
                    context.getMatrices().scale(textScale, textScale, zDepth);
                    int colorE = (int) (255 - (55 * exsPercent / 100));
                    context.drawTextWithShadow(
                        textRenderer, hearderText, 0, 0,
                        RGB.getColor(colorE, colorE, colorE, alphaColor)
                    );
                    context.getMatrices().pop();
                    moduleAreas.add(new ModuleArea(
                        lst,
                        drawX - spacing,
                        headerY,
                        textRenderer.getWidth(hearderText),
                        textRenderer.fontHeight
                    ));
                    float headerHeight = textRenderer.fontHeight * textScale;

                    List<String> options = lst.getOptions();
                    float optYOffset = headerHeight + spacing;
                    for (String element : options) {
                        float drawY = ySetOffset + drawOffsetY + (optYOffset * exsPercent / 100);
                        float width = textRenderer.getWidth(element) * textScale;
                        float height = textRenderer.fontHeight * textScale;
                        Text display = lst.getValue().equals(element) ? Text.literal(element).formatted(Formatting.BOLD) : Text.literal(element);

                        context.getMatrices().push();
                        context.getMatrices().translate(drawX, drawY, zDepth);
                        context.getMatrices().scale(textScale, textScale, zDepth);
                        int colorE2 = (int) ((alphaColor * animPercent / 100) * exsPercent / 100);
                        context.drawTextWithShadow(
                            textRenderer, display, 0, 0, RGB.getColor(255, 255, 255, colorE2)
                        );
                        context.getMatrices().pop();

                        moduleAreas.add(new ListArea(
                            lst,
                            element,
                            drawX,
                            drawY,
                            width,
                            height
                        ));

                        optYOffset += (textRenderer.fontHeight * textScale) + spacing;
                    }
                    ySetOffset += headerHeight + (((optYOffset) - headerHeight) * exsPercent / 100) + spacing;
                    continue;
                } else {
                    name = set.getName() + ": " + set.getValue();
                }
            } else if (set.getValue() != null) {
                name = set.getName() + ": " + set.getValue();
                if (set.getValue() instanceof Boolean) {
                    name = set.getName() + ": " + ((boolean) set.getValue() ? "1" : "0");
                    if ((boolean) set.getValue()) {
                        color = RGB.getColor(230, 255, 230, alphaColor);
                    } else {
                        color = RGB.getColor(255, 230, 230, alphaColor);
                    }
                }
            } else {
                name = set.getName();
                color = RGB.getColor(200, 200, 200, alphaColor);
            }

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

        ySetOffset = (ySetOffset - rectY) * setAnimPercent / 100;

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
        // хуйня
        for (Object mobule : moduleAreas) {
             if (mobule instanceof ListArea area) {
                if (mouseX >= area.x
                        && mouseX <= area.x + area.width
                        && mouseY >= area.y
                        && mouseY <= area.y + area.height) {
                    return area;
                }
            } else if (mobule instanceof ModuleArea area) {
                if (mouseX >= area.x
                        && mouseX <= area.x + area.width
                        && mouseY >= area.y
                        && mouseY <= area.y + area.height) {
                    return area.module;
                }
            }
        }
        return null;
    }

    private static class ListArea extends ModuleArea {
        private final String value;

        public ListArea(ListSetting listClass, String value, float x, float y, float width, float height) {
            super(listClass, x, y, width, height);
            this.value = value;
        }

        public void set() {
            if (module instanceof ListSetting lst) {
                lst.setValue(value);
            }
        }
    }

    private static class ModuleArea {
        public final Object module;
        public final float x;
        public final float y;
        public final float width;
        public final float height;

        public ModuleArea(Object module, float x, float y, float width, float height) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}