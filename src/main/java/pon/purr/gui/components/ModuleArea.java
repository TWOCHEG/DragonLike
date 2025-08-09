package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.boss.BossBar;
import org.lwjgl.glfw.GLFW;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.*;
import pon.purr.utils.KeyName;
import pon.purr.utils.Color;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;

public class ModuleArea extends RenderArea {
    public final Parent module;

    public final CategoryArea category;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    private float showKeysPercent = 0f;

    private float enablePercent = 0f;

    public float openPercent = 0f;
    private boolean open = false;

    private boolean binding = false;
    private float bindingPercent = 0f;
    private int pressKey = -1;

    public List<Integer> cancelButtons = List.of(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_DELETE
    );

    public ModuleArea(Parent module, CategoryArea category) {
        super();
        this.module = module;
        this.category = category;

        this.areas = getAreas(module.getSettings(), this);
    }

    public static List<RenderArea> getAreas(List<Setting> sets, ModuleArea module, SettingsGroupArea group) {
        List<RenderArea> areas = new LinkedList<>();
        for (Setting set : sets) {
            if (set.group != null && group == null) continue;
            Object o = group == null ? module : group;
            if (set instanceof SettingsGroup g) {
                areas.add(new SettingsGroupArea(g, module));
            } else if (set instanceof ListSetting l) {
                areas.add(new ListSettingsArea(l, o));
            } else if (set instanceof ColorSettings c) {
                areas.add(new ColorSettingsArea(c, o));
            } else if (set instanceof Header) {
                areas.add(new SettingsHeaderArea(set, o));
            } else if (set.getValue() instanceof Boolean) {
                areas.add(new BooleanSettingsArea(set, o));
            } else if (set.getValue() instanceof String) {
                areas.add(new StringSettingsArea(set, o));
            } else if (set.getValue() instanceof Number) {
                areas.add(new NumberSettingsArea(set, o));
            }
        }
        return areas;
    }
    public static List<RenderArea> getAreas(List<Setting> sets, ModuleArea module) {
        return getAreas(sets, module, null);
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        hovered = checkHovered(mouseX, mouseY);
        int settingsHeight = 0;
        for (RenderArea area : areas) {
            settingsHeight += area.height + bigPadding;
        }
        height = (int) (textRenderer.fontHeight + (padding * 2) + (settingsHeight * openPercent));

        context.enableScissor(
            startX,
            startY,
            startX + width,
            startY + height
        );

        int c = (int) (50 + ((50 * enablePercent) * (1 - openPercent)));

        if (openPercent > 0) {
            Render.fillPart(
                context,
                startX,
                startY,
                startX + width,
                startY + vertexRadius,
                Color.fromRGB(c, c, c, (50 + (40 * hoverPercent)) * category.visiblePercent),
                2,
                true
            );
            context.fill(
                startX,
                startY + vertexRadius,
                startX + width,
                startY + height - vertexRadius,
                Color.fromRGB(c, c, c, (50 + (40 * hoverPercent)) * category.visiblePercent)
            );
            Render.fillPart(
                context,
                startX,
                startY + height - vertexRadius,
                startX + width,
                startY + height,
                Color.fromRGB(c, c, c, (50 + (40 * hoverPercent)) * category.visiblePercent),
                2,
                false
            );
        } else {
            Render.fill(
                context,
                startX,
                startY,
                startX + width,
                startY + textRenderer.fontHeight + (padding * 2),
                Color.fromRGB(c, c, c, (50 + (40 * hoverPercent)) * category.visiblePercent),
                vertexRadius,
                2
            );
        }
        String name;
        if (bindingPercent > 0) {
            name = AnimHelper.getAnimText(module.getName(), pressKey == -1 ? "..." : KeyName.get(pressKey), bindingPercent);
        } else {
            name = showKeysPercent > 0 && module.getKeybind() != -1 ?
                AnimHelper.getAnimText(module.getName(), KeyName.get(module.getKeybind()), showKeysPercent) :
                module.getName();
        }
        if (bindingPercent == 0 && pressKey != -1) pressKey = -1;

        context.drawText(
            textRenderer,
            name,
            startX + (width / 2 - textRenderer.getWidth(name) / 2),
            startY + padding,
            Color.fromRGB(255, 255, 255, 200 * category.visiblePercent),
            false
        );
        if (enablePercent > 0) {
            context.drawHorizontalLine(
                (int) (startX + (width / 2) - (textRenderer.getWidth(name) / 2) * enablePercent),
                (int) (startX + (width / 2) + (textRenderer.getWidth(name) / 2) * enablePercent),
                startY + textRenderer.fontHeight + padding,
                Color.fromRGB(255, 255, 255, 200 * category.visiblePercent * enablePercent)
            );
        }

        int settingsStartY = startY + textRenderer.fontHeight + padding + bigPadding;
        if (openPercent != 0) {
            for (RenderArea area : areas) {
                area.render(
                    context,
                    startX + 2,
                    settingsStartY,
                    width - 4,
                    0,
                    mouseX, mouseY
                );
                settingsStartY += area.height + bigPadding;
            }
        }

        context.disableScissor();

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(!hovered, hoverPercent);
        showKeysPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS,
            showKeysPercent
        );
        enablePercent = AnimHelper.handleAnimValue(
            !module.getEnable(),
            enablePercent
        );
        bindingPercent = AnimHelper.handleAnimValue(
            !binding,
            bindingPercent
        );
        openPercent = AnimHelper.handleAnimValue(
            !open,
            openPercent
        );
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
        if (openPercent > 0.9f) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (cancelButtons.contains(keyCode)) {
                module.setKeybind(-1);
                binding = false;
            } else {
                module.setKeybind(keyCode);
                pressKey = keyCode;
                binding = false;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
