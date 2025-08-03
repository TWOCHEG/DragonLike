package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.Purr;
import pon.purr.modules.Parent;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

import java.util.*;

public class CategoryArea extends RenderArea {
    private final Purr.Categories name;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    public float visiblePercent = 0f;
    public boolean visibleReverse = true;

    public CategoryArea(LinkedList<Parent> modules, Purr.Categories name) {
        super();
        modules.forEach(m -> {
            areas.add(new ModuleArea(m, this));
        });
        this.name = name;
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        Float offset = 100 * (1 - visiblePercent);

        startY -= offset;

        int modulesStartY = startY + 26;
        int modulePadding = 2;

        // моджанги дети шлюх le le le le нахуй надо было убирать слои
        for (RenderArea m : areas) {
            height += m.height + modulePadding;
        }
        height += (modulesStartY - startY) + modulePadding;

        int alpha = (int) ((150 + (10 * hoverPercent)) * visiblePercent);
        hovered = checkHovered(mouseX, mouseY);
        int radius = 7;

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            (startY + height),
            RGB.getColor(0, 0, 0, alpha),
            radius,
            3
        );
        Render.fillPart(
            context,
            startX,
            startY,
            startX + width,
            startY + radius,
            RGB.getColor(0, 0, 0, 50 * visiblePercent),
            3, true
        );
        context.fillGradient(
            startX,
            (startY + radius),
            startX + width,
            startY + height - radius,
            RGB.getColor(0, 0, 0, 50 * visiblePercent),
            RGB.getColor(0, 0, 0, 0)
        );
        int nameStartY = (startY + 6);
        int nameContainerPadding = 3;
        Render.fill(
            context,
            startX - nameContainerPadding + (width / 2 - textRenderer.getWidth(name.name()) / 2),
            nameStartY - nameContainerPadding,
            startX + nameContainerPadding + (width / 2 + textRenderer.getWidth(name.name()) / 2),
            nameStartY + textRenderer.fontHeight + nameContainerPadding,
            RGB.getColor(0, 0, 0, 120 * visiblePercent),
            5,
            2
        );
        context.drawText(
            textRenderer,
            name.name(),
            startX + (width / 2 - textRenderer.getWidth(name.name()) / 2),
            nameStartY,
            RGB.getColor(255, 255, 255, 180 * visiblePercent),
            false
        );
        context.drawHorizontalLine(startX + nameContainerPadding + 12, startX + width - 20, (modulesStartY - 5), RGB.getColor(255, 255, 255, 50 * visiblePercent));

        for (RenderArea m : areas) {
            m.render(context, startX + modulePadding, modulesStartY, width - (modulePadding * 2), 0, mouseX, mouseY);
            modulesStartY += m.height + modulePadding;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(!hovered, hoverPercent);
        visiblePercent = AnimHelper.handleAnimValue(visibleReverse, visiblePercent, AnimHelper.AnimMode.EaseOut);
    }
}
