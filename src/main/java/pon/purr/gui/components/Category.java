package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.Parent;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

import java.util.*;

public class Category extends RenderArea {
    private final LinkedList<Module> modules;
    private final String name;

    private float hoverPercent = 0f;
    private boolean hovered = false;

    public float visiblePercent = 0f;
    public boolean visibleReverse = true;

    public Category(LinkedList<Parent> modules, String name) {
        super();
        LinkedList<Module> areas = new LinkedList<>();
        modules.forEach(m -> {
            areas.add(new Module(m, this));
        });
        this.modules = areas;
        this.name = name;
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        Float offset = 100 * (1 - visiblePercent);

        startY -= offset;

        int modulesStartY = startY + 26;
        int modulePadding = 2;

        context.drawHorizontalLine(startX + 20, startX + width - 20, (modulesStartY - 5), RGB.getColor(255, 255, 255, 180 * visiblePercent));
        // моджанги дети шлюх le le le le нахуй надо было убирать слои
        for (Module m : modules) {
            height += m.height + modulePadding;
        }
        height += modulesStartY - startY;

        int alpha = (int) ((150 + (10 * (1 - hoverPercent))) * visiblePercent);
        hovered = checkHovered(mouseX, mouseY);
        int radius = width / 20;

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            (startY + height),
            RGB.getColor(0, 0, 0, alpha),
            width / 20,
            3
        );
        Render.fill1(
            context,
            startX,
            startY,
            startX + width,
            startY + radius,
            RGB.getColor(0, 0, 0, 50 * visiblePercent),
            3
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
            startX - nameContainerPadding + (width / 2 - textRenderer.getWidth(name) / 2),
            nameStartY - nameContainerPadding,
            startX + nameContainerPadding + (width / 2 + textRenderer.getWidth(name) / 2),
            nameStartY + textRenderer.fontHeight + nameContainerPadding,
            RGB.getColor(0, 0, 0, 120 * visiblePercent),
            width / 30,
            3
        );
        context.drawCenteredTextWithShadow(
            textRenderer,
            name,
            startX + (width / 2),
            nameStartY,
            RGB.getColor(255, 255, 255, 180 * visiblePercent)
        );

        for (Module m : modules) {
            m.render(context, startX + modulePadding * 2, modulesStartY, width - (modulePadding * 4), 0, mouseX, mouseY);
            modulesStartY += m.height + modulePadding;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(!hovered, hoverPercent * 100) / 100;
        visiblePercent = AnimHelper.handleAnimValue(
            visibleReverse,
            visiblePercent * 100,
            AnimHelper.AnimMode.EaseOut
        ) / 100;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Module m : modules) {
            if (checkHovered(m, mouseX, mouseY)) {
                return m.mouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }
}
