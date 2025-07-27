package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.Parent;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

public class Module extends RenderArea {
    private final Parent module;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    public Module(Parent module) {
        super();
        this.module = module;
    }

    @Override
    public void onRender(DrawContext context, int startX, int startY, int width, int height, float visiblePercent, double mouseX, double mouseY) {
        hovered = checkHovered(mouseX, mouseY);

        int textPadding = 2;

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            startY + textRenderer.fontHeight + (textPadding * 2),
            RGB.getColor(0, 0, 0, (50 + (40 * (1 - hoverPercent))) * visiblePercent),
            width / 30,
            3
        );

        context.drawCenteredTextWithShadow(
            textRenderer,
            module.getName(),
            startX + width / 2,
            startY + textPadding,
            RGB.getColor(255, 255, 255, 255 * visiblePercent)
        );
        height = textRenderer.fontHeight + (textPadding * 2);

        super.onRender(context, startX, startY, width, height, visiblePercent, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(hovered, hoverPercent * 100) / 100;
    }
}
