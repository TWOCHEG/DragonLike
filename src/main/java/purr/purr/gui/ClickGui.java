// местая зона отчуждения, просьба не заходить без подготовки (хотя бы моральной)
package purr.purr.gui;

import net.minecraft.structure.rule.blockentity.AppendLootRuleBlockEntityModifier;
import purr.purr.modules.ModuleManager;
import purr.purr.modules.settings.Group;
import purr.purr.modules.settings.ListSetting;
import purr.purr.modules.settings.Setting;
import purr.purr.modules.ui.Gui;
import purr.purr.utils.RGB;
import purr.purr.modules.Parent;

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

import purr.purr.utils.AnimHelper;

import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGui extends Screen {
    private final Map<String, List<Parent>> modules;
    private Gui guiModule;

    private static final String hintsText = "← ↑ ↓ → - move gui\nleft shift - percent binds\nmouse middle - bind module";

    private List<Object> moduleAreas = new ArrayList<>();

    private Parent bindingModule = null;

    private Setting inputSet = null;
    private String inputText = null;
    private Float inputAnim = 0f;
    private Boolean inputAnimReverse = true;

    private float animPercent = 0;
    private boolean animReverse = false;

    private float showKeybind = 0;

    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private float xMove = 0;
    private float yMove = 0;

    private final Screen previous;

    private Map<Object, Float> hoverAnim = new HashMap<>();
    private Map<Object, Boolean> hovemrAnimReverse = new HashMap<>();

    private Map<Object, Float> bindAnim = new HashMap<>();
    private Map<Object, Boolean> bindAnimReverse = new HashMap<>();

    private Map<Object, Float> setAnim = new HashMap<>();
    private Map<Object, Boolean> setAnimReverse = new HashMap<>();

    private Map<Object, Float> exsAnim = new HashMap<>();
    private Map<Object, Boolean> exsAnimReverse = new HashMap<>();

    private Map<Object, Float> setVisAnim = new HashMap<>();
    private Map<Object, Boolean> setVisAnimReverse = new HashMap<>();

    public ClickGui(Screen previous, ModuleManager moduleManager, Gui guiModule, LinkedList<Map<?, ?>> animSave) {
        super(Text.literal("Purr Gui"));
        this.previous = previous;
        this.modules = moduleManager.getModules();
        this.guiModule = guiModule;

        if (animSave != null && animSave.size() == 6) {
            setAnim = (Map<Object, Float>) animSave.get(0);
            setAnimReverse = (Map<Object, Boolean>) (animSave.get(1));
            exsAnim = (Map<Object, Float>) (animSave.get(2));
            exsAnimReverse = (Map<Object, Boolean>) (animSave.get(3));
            setVisAnim = (Map<Object, Float>) (animSave.get(4));
            setVisAnimReverse = (Map<Object, Boolean>) (animSave.get(5));
        }
    }

    public void closeGui() {
        closeAll();

        animReverse = true;
        LinkedList<Map<?, ?>> saveList = new LinkedList<>();

        saveList.add(setAnim);
        saveList.add(setAnimReverse);
        saveList.add(exsAnim);
        saveList.add(exsAnimReverse);
        saveList.add(setVisAnim);
        saveList.add(setVisAnimReverse);
        guiModule.animSave = saveList;
    }

    public void closeAll() {
        inputSet = null;
        inputText = null;
        inputAnim = 0f;
    }
    public boolean checkActive() {
        return (
            inputSet != null
        );
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

        if (guiModule.runProcess.getValue()) {
            new Thread(() -> {
                animHandler();
            }, "Purr-animations").start();
        } else {
            animHandler();
        }

        moduleAreas.clear();

        float screenHeight = context.getScaledWindowHeight();
        float screenWidth = context.getScaledWindowWidth();

        handleGuiImage(context, screenWidth, screenHeight);

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

        // параметры
        float yStart = (10 + yMove) * animPercent / 100;
        int baseTextHeight = textRenderer.fontHeight;
        int spacing = 10;
        int spacingColumns = 10;
        int numCols = modules.size();
        int columnWidth = Math.min(70, (width - spacingColumns * (numCols - 1)) / numCols);
        int totalColsWidth = numCols * columnWidth + (numCols - 1) * spacingColumns;
        float xColStart = (((width + xMove) - totalColsWidth) / 2);

        for (Map.Entry<String, List<Parent>> entry : modules.entrySet()) {
            String category = entry.getKey();
            if (category == null) continue;
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

            float maxWidth = 0;

            for (Parent module : list) {
                if (module.getName() == null) continue;
                boolean hovered = !(
                    (
                        mouseX >= xColStart
                        && mouseX <= xColStart + columnWidth
                        && mouseY >= yOffset
                        && mouseY <= yOffset + baseTextHeight
                    ) || (
                        setAnim.get(module) != null
                    )
                );

                hovemrAnimReverse.put(module, hovered);
                if (!hoverAnim.containsKey(module)) {
                    hoverAnim.put(module, 0f);
                }
                float hoverPercent = hoverAnim.get(module);

                float maxScaleDelta = 0.2f;
                float scale = 1.0f + (maxScaleDelta * hoverPercent / 100f);
                float scaledHeight = baseTextHeight * scale;

                // Готовим текст
                int keyBind = module.getKeybind();
                String moduleName = module.getName();
                String name = moduleName;
                if (showKeybind > 1) {
                    if (keyBind != -1) {
                        String key = keyName(keyBind);
                        name = AnimHelper.getAnimText(moduleName, key, (int) showKeybind);
                    }
                } else if (bindAnim.containsKey(module) && bindAnim.getOrDefault(module, 0f) > 0f) {
                    name = AnimHelper.getAnimText(moduleName, "...", bindAnim.getOrDefault(module, 0f).intValue());
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
                float winWidth = 0;
                float xDifference = 0;
                if (setAnim.containsKey(module) && !module.getSettings().isEmpty()) {
                    List<Float> drawSettingsResult = drawSettings(module, sets, context, yOffset, xColStart, scale, baseTextHeight);
                    winHeight = drawSettingsResult.getFirst();
                    winWidth = drawSettingsResult.get(1);
                } else if (module.getSettings().isEmpty() && setAnim.containsKey(module)) {
                    setAnimReverse.put(module, true);
                    // эта переменная может быть от 15
                    float percent = setAnim.get(module);

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

                maxWidth = Math.max(maxWidth, textRenderer.getWidth(name) * scale);
                maxWidth = Math.max(maxWidth, winWidth);

                yOffset += scaledHeight + spacing + winHeight;
            }

            float finalXOffset = columnWidth;
            if (finalXOffset < maxWidth) {
                finalXOffset = maxWidth;
            }
            xColStart += finalXOffset + spacingColumns;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputSet != null) {
            inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Object obj = getModuleUnderMouse((int) mouseX, (int) mouseY);
        if (obj == null && checkActive()) {
            closeAll();
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (obj instanceof ListArea lst) {
                lst.set();
                return true;
            } else if (obj instanceof ListSetting set) {
                int i = set.getOptions().indexOf(set.getValue());
                i += 1;
                if (i > set.getOptions().size() - 1) {
                    i = 0;
                }
                set.setValue(set.getOptions().get(i));
                return true;
            } else if (obj instanceof Parent module) {
                module.setEnable(!module.getEnable());
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (obj instanceof ListSetting || obj instanceof ListArea) {
                Setting set = (Setting) (obj instanceof ListArea ? ((ListArea) obj).module : obj);
                if (!exsAnim.containsKey(set)) {
                    exsAnim.put(set, 0.0F);
                    exsAnimReverse.put(set, false);
                } else {
                    exsAnimReverse.put(set, true);
                }
                return true;
            } else if (obj instanceof Parent module) {
                if (!setAnim.containsKey(module)) {
                    setAnim.put(module, 0.0F);
                    setAnimReverse.put(module, false);
                } else {
                    setAnimReverse.put(module, true);
                }
                return true;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (obj instanceof Parent module) {
                bindingModule = module;
                bindAnimReverse.put(bindingModule, false);
                bindAnim.put(bindingModule, 0f);
                for (Object key : bindAnim.keySet()) {
                    if (key.equals(bindingModule)) {
                        continue;
                    }
                    bindAnimReverse.put(key, true);
                }
                return true;
            }
        }

        if (obj instanceof IntArea area) {
            float x = ((Double) mouseX).floatValue();
            area.set(x);
        }

        if (obj instanceof Group group) {
            group.setOpen(!group.isOpen());
            return true;
        }

        if (obj instanceof Setting set) {
            if (set.getValue() instanceof Boolean) {
                set.setValue(!(Boolean) set.getValue());
            } else if (set.getValue() instanceof String || set.getValue() instanceof Integer || set.getValue() instanceof Float) {
                inputSet = set;
                inputText = set.getValue() instanceof String ? (String) set.getValue() : set.getValue().toString();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Object obj = getModuleUnderMouse(mouseX, mouseY);
        if (obj instanceof IntArea area) {
            area.set((float) mouseX);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (checkActive() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeAll();
            return true;
        }
        if (bindingModule != null) {
            int value = (keyCode == GLFW.GLFW_KEY_ESCAPE) ? -1 : keyCode;
            bindingModule.setKeybind(value);
            bindAnimReverse.put(bindingModule, true);
            bindingModule = null;
            return true;
        }
        if (inputSet != null && keyCode == GLFW.GLFW_KEY_ENTER) {
            if (inputSet.getValue() instanceof Integer) {
                try {
                    int value = Integer.parseInt(inputText);
                    inputSet.setValue(value);
                } catch (NumberFormatException ignored) {}
            } else if (inputSet.getValue() instanceof Float) {
                try {
                    float value = Float.parseFloat(inputText.replace(",", "."));
                    inputSet.setValue(value);
                } catch (NumberFormatException ignored) {}
            } else {
                inputSet.setValue(inputText);
            }
            inputSet = null;
            inputText = null;
            inputAnim = 0f;
            return true;
        }
        if (inputSet != null && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!inputText.isEmpty()) {
            inputText = inputText.substring(0, inputText.length() - 1);
            }
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
            closeGui();
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

    private void animHandler() {
        animPercent = AnimHelper.handleAnimValue(animReverse, animPercent);

        long window = client.getWindow().getHandle();
        boolean shiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        showKeybind = AnimHelper.handleAnimValue(!shiftDown, showKeybind);

        if (inputSet != null) {
            inputAnim = AnimHelper.handleAnimValue(inputAnimReverse, inputAnim);
            if (inputAnim == 100f) {
                inputAnimReverse = true;
            } else if (inputAnim == 0f) {
                inputAnimReverse = false;
            }
        }

        AnimHelper.handleMapAnim(hoverAnim, hovemrAnimReverse, false);
        AnimHelper.handleMapAnim(bindAnim, bindAnimReverse);
        AnimHelper.handleMapAnim(setAnim, setAnimReverse);
        AnimHelper.handleMapAnim(exsAnim, exsAnimReverse);
        AnimHelper.handleMapAnim(setVisAnim, setVisAnimReverse, false);
    }

    public List<Float> drawSettings(
        Parent module,
        List<Setting<?>> sets,
        DrawContext context,
        float yOffset,
        float xStart,
        float scale,
        int baseTextHeight
    ) {
        // параметры
        int zDepth = 3;
        float setAnimPercent = animPercent * setAnim.get(module) / 100;
        int paddingBelowText = 5;
        int rectY = (int) (yOffset + baseTextHeight * scale + paddingBelowText);
        int spacing = 5;
        xStart += spacing;
        float drawOffsetY = 10 * (setAnimPercent - 100) / 100f;

        float ySetOffset = paddingBelowText + rectY;

        float maxWidth = 0f;
        Group currentGroup;
        Group lastDrawGroup = null;

        for (Setting set : sets) {
            float xColStart = xStart;
            int alphaColor = (int) (255 * setAnimPercent / 100);
            float textScale = guiModule.settingsScale.getValue();
            int color;
            String name;
            currentGroup = set.getGroup();

            if (currentGroup != null && lastDrawGroup != currentGroup) {
                Text hearderText = Text.literal(currentGroup.getName() + (currentGroup.isOpen() ? " +" : " -"));
                context.getMatrices().push();
                context.getMatrices().translate(xColStart, ySetOffset + (10 * (setAnimPercent - 100) / 100f), zDepth);
                context.getMatrices().scale(textScale, textScale, zDepth);
                int colorE = (int) (175 * setAnimPercent / 100);
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

                maxWidth = Math.max(maxWidth, textRenderer.getWidth(hearderText));

                ySetOffset += (textRenderer.fontHeight * textScale) + spacing;
                lastDrawGroup = currentGroup;
            }
            if (!setVisAnim.containsKey(set)) {
                if (currentGroup != null && !currentGroup.isOpen()) {
                    setVisAnim.put(set, 0.0f);
                    setVisAnimReverse.put(set, true);
                } else {
                    boolean meetsCondition = set.getVisible();
                    boolean isGroupOpenOrNoGroup = (currentGroup == null || currentGroup.isOpen());
                    boolean initialReverse = !(meetsCondition && isGroupOpenOrNoGroup);

                    setVisAnim.put(set, initialReverse ? 0.0f : 100.0f);
                    setVisAnimReverse.put(set, initialReverse);
                }
            }
            if (currentGroup != null) {
                if (!currentGroup.isOpen() && !setVisAnimReverse.getOrDefault(set, false)) {
                    setVisAnimReverse.put(set, true);
                } else if (currentGroup.isOpen() && setVisAnimReverse.getOrDefault(set, true)) {
                    setVisAnim.put(set, setVisAnim.getOrDefault(set, 0.0f));
                    setVisAnimReverse.put(set, false);
                }
            }
            boolean isGroupOpenOrNoGroup = currentGroup == null || currentGroup.isOpen();
            if (isGroupOpenOrNoGroup) {
                boolean meetsCondition = set.getVisible();

                if (!meetsCondition && !setVisAnimReverse.getOrDefault(set, false)) {
                    setVisAnimReverse.put(set, true);
                } else if (meetsCondition && setVisAnimReverse.getOrDefault(set, true)) {
                    setVisAnim.put(set, setVisAnim.getOrDefault(set, 0.0f));
                    setVisAnimReverse.put(set, false);
                }
            }
            float visAnimPercent = setVisAnim.getOrDefault(set, 100.0f) * setAnimPercent / 100;
            if (visAnimPercent == 0.0f) continue;

            if (currentGroup != null) {
                xColStart += spacing;
                context.getMatrices().push();
                context.getMatrices().translate(0, 0, zDepth);
                context.getMatrices().scale(1, 1, zDepth);
                context.fill(
                    (int) (xStart + 1),
                    (int) (ySetOffset),
                    (int) (xStart),
                    (int) (ySetOffset + textRenderer.fontHeight),
                    RGB.getColor(175, 175, 175, (int) (200 * visAnimPercent / 100))
                );
                context.getMatrices().pop();
            }

            alphaColor = (int) (alphaColor * visAnimPercent / 100);

            if (set instanceof ListSetting lst) {
                if (exsAnim.get(lst) != null && exsAnim.get(lst) != 0.0f) {
                    float exsPercent = visAnimPercent * exsAnim.getOrDefault(lst, 0.0f) / 100;
                    float drawX = xColStart + spacing;
                    float headerY = ySetOffset + drawOffsetY;
                    Text hearderText = Text.literal(set.getName() + ": " + AnimHelper.getAnimText((String) lst.getValue(), "", (int) exsPercent));
                    context.getMatrices().push();
                    context.getMatrices().translate(drawX - spacing, headerY, zDepth);
                    context.getMatrices().scale(textScale, textScale, zDepth);
                    int colorE = (int) (255 - (55 * exsPercent / 100));
                    context.drawTextWithShadow(
                        textRenderer, hearderText, 0, 0,
                        RGB.getColor(colorE, colorE, colorE, alphaColor)
                    );
                    context.getMatrices().pop();
                    if (visAnimPercent == 100f) {
                        moduleAreas.add(new ModuleArea(
                            lst,
                            drawX - spacing,
                            headerY,
                            textRenderer.getWidth(hearderText) * textScale,
                            textRenderer.fontHeight
                        ));
                    }
                    maxWidth = Math.max(maxWidth, (textRenderer.getWidth(hearderText) * textScale) * exsPercent / 100);
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

                        if (exsPercent == 100f) {
                            moduleAreas.add(new ListArea(
                                lst,
                                element,
                                drawX,
                                drawY,
                                width,
                                height
                            ));
                        }

                        maxWidth = Math.max(maxWidth, width * exsPercent / 100);

                        optYOffset += (textRenderer.fontHeight * textScale) + spacing;
                    }
                    ySetOffset += (headerHeight + (((optYOffset) - headerHeight) * exsPercent / 100) + spacing) * visAnimPercent / 100;
                    continue;
                } else {
                    name = set.getName() + ": " + set.getValue();
                    color = RGB.getColor(255, 255, 255, alphaColor);
                }
            } else if (set.getValue() != null) {
                name = set.getName() + ": " + set.getValue();
                if (set.getValue() instanceof String && ((String) set.getValue()).isEmpty()) {
                    name = set.getName() + ": ...";
                }
                color = RGB.getColor(255, 255, 255, alphaColor);
                if (set.getValue() instanceof Boolean) {
                    name = set.getName() + ": " + ((boolean) set.getValue() ? "1" : "0");
                    if ((boolean) set.getValue()) {
                        color = RGB.getColor(210, 255, 230, alphaColor);
                    } else {
                        color = RGB.getColor(255, 210, 230, alphaColor);
                    }
                }
                if (inputSet != null && inputSet.equals(set)) {
                    name = set.getName() + ": " + (inputText != null && !inputText.isEmpty() ? inputText : "...");
                    color = RGB.getColor(255,  255, 255, (int) (255 - (200 * inputAnim / 100)));
                }
            } else {
                name = set.getName();
                color = RGB.getColor(175, 175, 175, alphaColor);
            }

            float drawX = xColStart;
            float drawY = ySetOffset + drawOffsetY;
            float width = (textRenderer.getWidth(name) * textScale) + (currentGroup != null ? spacing * 2 : spacing);
            float height = textRenderer.fontHeight * textScale;
            Text display = Text.literal(name);

            context.getMatrices().push();
            context.getMatrices().translate(drawX, drawY, zDepth);
            context.getMatrices().scale(textScale, textScale, zDepth);
            context.drawTextWithShadow(textRenderer, display, 0, 0, color);
            context.getMatrices().pop();

            if (visAnimPercent == 100f) {
                moduleAreas.add(new ModuleArea(
                    set,
                    drawX,
                    drawY,
                    width,
                    height
                ));
            }

            drawY += textRenderer.fontHeight;

            if (set.getValue() instanceof Integer || set.getValue() instanceof Float) {
                double currentValue = set.getValue() instanceof Float
                        ? ((Float) set.getValue()).doubleValue()
                        : ((Integer) set.getValue()).doubleValue();

                double currentMin = set.min instanceof Float
                        ? ((Float) set.min).doubleValue()
                        : ((Integer) set.min).doubleValue();

                double currentMax = set.max instanceof Float
                        ? ((Float) set.max).doubleValue()
                        : ((Integer) set.max).doubleValue();
                double totalWidth = (xStart + (spacing * 2) + Math.max(textRenderer.getWidth(set.getName()), 50) * textScale) - xColStart;
                double ratio = 0.0;
                if (currentMax - currentMin != 0) {
                    ratio = (currentValue - currentMin) / (currentMax - currentMin);
                }
                int filledWidth = (int) (totalWidth * ratio);
                maxWidth = Math.max(maxWidth, (filledWidth + spacing) * visAnimPercent / 100);

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, zDepth + 1);
                context.getMatrices().scale(1, 1, zDepth + 1);
                context.fill(
                    (int) xColStart,
                    (int) (drawY + 2),
                    (int) (xStart + totalWidth),
                    (int) (drawY + 3),
                    RGB.getColor(175, 175, 175, (int) (200 * (visAnimPercent) / 100))
                );
                context.fill(
                    (int) xColStart,
                    (int) (drawY + 2),
                    (int) (xColStart + filledWidth),
                    (int) (drawY + 3),
                    RGB.getColor(220, 220, 220, (int) (200 * (visAnimPercent) / 100))
                );
                context.getMatrices().pop();

                if (visAnimPercent == 100f) {
                    moduleAreas.add(new IntArea(set, xColStart, drawY, (float) totalWidth + (spacing * 2), 6));
                }
                height += 4;
            }

            maxWidth = Math.max(maxWidth, width * visAnimPercent / 100);

            ySetOffset += (height + spacing) * visAnimPercent / 100;
        }

        if (guiModule.setBg.getValue()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, zDepth);
            context.getMatrices().scale(1, 1, zDepth);
            context.fill(
                (int) (xStart + maxWidth + spacing),
                (int) (ySetOffset + drawOffsetY),
                (int) (xStart - spacing),
                (int) (rectY + drawOffsetY),
                RGB.getColor(0, 0, 0, (int) (guiModule.setBgAlpha.getValue() * setAnimPercent / 100))
            );
            context.getMatrices().pop();
        }

        ySetOffset = (ySetOffset - rectY) * setAnimPercent / 100;

        return new ArrayList<>(List.of(ySetOffset, (maxWidth + spacing) * setAnimPercent / 100));
    }

    private void handleGuiImage(DrawContext context, float screenWidth, float screenHeight) {
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
                float fixedSize = screenMin * guiModule.imgSize.getValue();

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
    }

    private static String keyName(int keyKode) {
        InputUtil.Key key = InputUtil.fromKeyCode(keyKode, GLFW.glfwGetKeyScancode(keyKode));
        return key.getTranslationKey()
                .substring(key.getTranslationKey().lastIndexOf('.') + 1)
                .toUpperCase();
    }

    private Object getModuleUnderMouse(float mouseX, float mouseY) {
        for (Object mobule : moduleAreas) {
             if (mobule instanceof ModuleArea area) {
                if (mouseX >= area.x
                        && mouseX <= area.x + area.width
                        && mouseY >= area.y
                        && mouseY <= area.y + area.height) {
                    return (
                        area instanceof ListArea ||
                        area instanceof IntArea
                    ) ? area : area.module;
                }
            }
        }
        return null;
    }
    private Object getModuleUnderMouse(double mouseX, double mouseY) {
        return getModuleUnderMouse(((Double) mouseX).floatValue(), ((Double) mouseY).floatValue());
    }

    private static class IntArea extends ModuleArea {
        public IntArea(Setting set, float x, float y, float width, float height) {
            super(set, x, y - 2, width, height + 2);
        }

        public void set(float mouseX) {
            if (module instanceof Setting set) {
                float normalizedX = mouseX - x;
                normalizedX = Math.max(0f, Math.min(normalizedX, width));

                double currentMin = ((Number) set.min).doubleValue();
                double currentMax = ((Number) set.max).doubleValue();

                double ratio = width > 0 ? normalizedX / width : 0;

                double calculatedValue = currentMin + (currentMax - currentMin) * ratio;

                if (set.getValue() instanceof Integer) {
                    set.setValue((int) Math.round(calculatedValue));
                } else if (set.getValue() instanceof Float) {
                    set.setValue((float) (Math.round(calculatedValue * 10.0) / 10.0)); // Округление до 1 знака
                }
            }
        }
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