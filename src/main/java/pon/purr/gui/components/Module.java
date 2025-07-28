package pon.purr.gui.components;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.purr.events.impl.EventKeyPress;
import pon.purr.modules.Parent;
import pon.purr.utils.KeyName;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

import java.util.List;

public class Module extends RenderArea {
    private final Parent module;
    private final Category category;

    private int textPadding = 2;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    private float showKeysPercent = 0f;

    private float enablePercent = 0f;

    private boolean binding = false;
    private float bindingPercent = 0f;
    private int pressKey = -1;

    private List<Integer> cancelButtons = List.of(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_DELETE
    );

    public Module(Parent module, Category category) {
        super();
        this.module = module;
        this.category = category;
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        hovered = checkHovered(mouseX, mouseY);

        int c = (int) (50 + (50 * enablePercent));

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            startY + textRenderer.fontHeight + (textPadding * 2),
            RGB.getColor(c, c, c, (50 + (40 * hoverPercent)) * category.visiblePercent),
            width / 30,
            2
        );
        String name;
        if (bindingPercent > 0) {
            name = AnimHelper.getAnimText(module.getName(), pressKey == -1 ? "..." : KeyName.get(pressKey), bindingPercent);
        } else {
            name = showKeysPercent > 0 && module.getKeybind() != -1 ?
                AnimHelper.getAnimText(module.getName(), KeyName.get(module.getKeybind()), showKeysPercent) :
                module.getName();
        }
        if (bindingPercent == 0 && pressKey != -1) pressKey = -1;

        context.drawCenteredTextWithShadow(
            textRenderer,
            name,
            startX + width / 2,
            startY + textPadding,
            RGB.getColor(255, 255, 255, 200 * category.visiblePercent)
        );
        if (enablePercent > 0) {
            context.drawHorizontalLine(
                (int) (startX + (width / 2) - (textRenderer.getWidth(name) / 2) * enablePercent),
                (int) (startX + (width / 2) + (textRenderer.getWidth(name) / 2) * enablePercent),
                startY + textRenderer.fontHeight + textPadding,
                RGB.getColor(255, 255, 255, 200 * category.visiblePercent * enablePercent)
            );
        }
        height = textRenderer.fontHeight + (textPadding * 2);

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(!hovered, hoverPercent * 100) / 100;
        showKeysPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS,
            showKeysPercent * 100
        ) / 100;
        enablePercent = AnimHelper.handleAnimValue(
            !module.getEnable(),
            enablePercent * 100
        ) / 100;
        bindingPercent = AnimHelper.handleAnimValue(
            !binding,
            bindingPercent * 100
        ) / 100;
    }

    @Override
    public boolean areaMouseClicked(double mouseX, double mouseY, int button) {
        if (y + (textRenderer.fontHeight + textPadding * 2) > mouseY) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                module.toggle();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                pressKey = -1;
                binding = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (binding) {
            binding = false;
        }
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
        return false;
    }
}
