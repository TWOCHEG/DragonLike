package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.modules.Parent;
import pon.main.modules.settings.*;
import pon.main.utils.KeyName;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;

public class ModuleArea extends RenderArea {
    public final Parent module;

    public final CategoryArea category;

    private float hoverFactor = 0f;
    private boolean hovered = false;

    private float showKeysFactor = 0f;

    private float enableFactor = 0f;

    private boolean open = false;

    private boolean binding = false;
    private float bindingAnimFactor = 0f;
    private int pressKey = -1;

    public ModuleArea(Parent module, CategoryArea category) {
        super();
        this.module = module;
        this.category = category;

        this.showFactor = 0;

        this.areas = getAreas(module.getSettings(), this);
    }

    public static List<RenderArea> getAreas(List<Setting> sets, RenderArea parent) {
        List<RenderArea> areas = new LinkedList<>();
        for (Setting set : sets) {
            if (!(parent instanceof SetsGroupArea || parent instanceof SetsHGroupArea)) {
                if (set.group != null) continue;
            }

            if (set instanceof HGroup g) {
                areas.add(new SetsHGroupArea(g, parent));
            } else if (set instanceof Group g) {
                areas.add(new SetsGroupArea(g, parent));
            } else if (set instanceof SetsList l) {
                areas.add(new SetsListArea(l, parent));
            } else if (set instanceof ColorSet c) {
                areas.add(new ColorSetArea(c, parent));
            } else if (set instanceof KeyButton kb) {
                areas.add(new KeyButtonArea(kb, parent));
            } else if (set instanceof Header h) {
                areas.add(new HeaderSetArea(h, parent));
            } else if (set.getValue() instanceof Boolean) {
                areas.add(new BooleanSetArea(set, parent));
            } else if (set.getValue() instanceof String) {
                areas.add(new StringSetArea(set, parent));
            } else if (set.getValue() instanceof Number) {
                areas.add(new NumberSetArea(set, parent));
            }
        }
        return areas;
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        hovered = checkHovered(mouseX, mouseY);
        int settingsHeight = 0;
        for (RenderArea area : areas) {
            settingsHeight += area.height + bigPadding;
        }
        height = (int) (textRenderer.fontHeight + (padding * 2) + (settingsHeight * showFactor));

        context.enableScissor(
            startX,
            startY,
            startX + width,
            startY + height
        );

        int c = (int) (50 + ((50 * enableFactor) * (1 - showFactor)));

        if (showFactor > 0) {
            Render2D.fillPart(
                context,
                startX,
                startY,
                startX + width,
                startY + vertexRadius,
                ColorUtils.fromRGB(c, c, c, (50 + (40 * hoverFactor)) * parentArea.showFactor),
                2,
                true
            );
            context.fill(
                startX,
                startY + vertexRadius,
                startX + width,
                startY + height - vertexRadius,
                ColorUtils.fromRGB(c, c, c, (50 + (40 * hoverFactor)) * parentArea.showFactor)
            );
            Render2D.fillPart(
                context,
                startX,
                startY + height - vertexRadius,
                startX + width,
                startY + height,
                ColorUtils.fromRGB(c, c, c, (50 + (40 * hoverFactor)) * parentArea.showFactor),
                2,
                false
            );
        } else {
            Render2D.fill(
                context,
                startX,
                startY,
                startX + width,
                startY + textRenderer.fontHeight + (padding * 2),
                ColorUtils.fromRGB(c, c, c, (50 + (40 * hoverFactor)) * parentArea.showFactor),
                vertexRadius,
                2
            );
        }
        String name;
        if (bindingAnimFactor > 0) {
            name = AnimHelper.getAnimText(module.getName(), pressKey == -1 ? "..." : KeyName.get(pressKey), bindingAnimFactor);
        } else {
            name = showKeysFactor > 0 && module.getKeybind() != -1 ?
                AnimHelper.getAnimText(module.getName(), KeyName.get(module.getKeybind()), showKeysFactor) :
                module.getName();
        }
        if (bindingAnimFactor == 0 && pressKey != -1) pressKey = -1;

        context.drawText(
            textRenderer,
            name,
            startX + (width / 2 - textRenderer.getWidth(name) / 2),
            startY + padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * parentArea.showFactor),
            false
        );
        if (enableFactor > 0) {
            context.drawHorizontalLine(
                (int) (startX + (width / 2) - (textRenderer.getWidth(name) / 2) * enableFactor),
                (int) (startX + (width / 2) + (textRenderer.getWidth(name) / 2) * enableFactor),
                startY + textRenderer.fontHeight + padding,
                ColorUtils.fromRGB(255, 255, 255, 200 * parentArea.showFactor * enableFactor)
            );
        }

        int settingsStartY = startY + textRenderer.fontHeight + padding + bigPadding;
        if (showFactor != 0) {
            for (RenderArea area : areas) {
                area.render(
                    context,
                    startX + 2,
                    settingsStartY,
                    width - 4,
                    0,
                    mouseX, mouseY
                );
                if (area instanceof SetsGroupArea sga) {
                    settingsStartY += sga.height + (bigPadding * sga.visibleFactor);
                } else {
                    settingsStartY += area.height + (bigPadding * area.showFactor);
                }
            }
        }

        context.disableScissor();

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverFactor = AnimHelper.handle(!hovered, hoverFactor);
        showKeysFactor = AnimHelper.handle(
            GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS,
            showKeysFactor
        );
        enableFactor = AnimHelper.handle(!module.getEnable(), enableFactor);
        bindingAnimFactor = AnimHelper.handle(!binding, bindingAnimFactor);
        showFactor = AnimHelper.handle(!open, showFactor * category.showFactor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (binding) {
            binding = false;
        }
        if (checkHovered(x, y, width, textRenderer.fontHeight + padding * 2, mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                pressKey = -1;
                binding = true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !areas.isEmpty()) {
                open = !open;
            }
            return true;
        }
        if (showFactor > 0.9f) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (Main.cancelButtons.contains(keyCode)) {
                module.setKeybind(-1);
            } else {
                module.setKeybind(keyCode);
                pressKey = keyCode;
            }
            binding = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
