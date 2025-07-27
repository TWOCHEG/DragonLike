package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pon.purr.modules.Parent;
import pon.purr.utils.KeyName;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

public class Module extends RenderArea {
    private final Parent module;
    private final Category category;

    private int textPadding = 2;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    private float showKeysPercent = 0f;

    private float enablePercent = 0f;

    public Module(Parent module, Category category) {
        super();
        this.module = module;
        this.category = category;
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        hovered = checkHovered(mouseX, mouseY);

        int c = (int) (0 + (100 * enablePercent));

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
        String name = showKeysPercent > 0 && module.getKeybind() != -1 ?
        AnimHelper.getAnimText(module.getName(), KeyName.get(module.getKeybind()), showKeysPercent) :
        module.getName();
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
                RGB.getColor(255, 255, 255, 200 * category.visiblePercent)
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
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (y + (textRenderer.fontHeight + textPadding * 2) > mouseY) {
            if (button == 0) {
                module.toggle();
                return true;
            }
        }
        return false;
    }
}
