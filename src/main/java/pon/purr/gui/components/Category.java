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

    public Category(LinkedList<Parent> modules, String name) {
        super();
        LinkedList<Module> areas = new LinkedList<>();
        modules.forEach(m -> {
            areas.add(new Module(m));
        });
        this.modules = areas;
        this.name = name;
    }

    @Override
    public void onRender(DrawContext context, int startX, int startY, int width, int height, float visiblePercent, double mouseX, double mouseY) {
        Float offset = 100 * (1 - visiblePercent);

        int modulesStartY = startY + 26;
        int modulePadding = 2;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, 0 - offset);

        context.drawHorizontalLine(startX + 10, startX + width - 10, (modulesStartY - 5), RGB.getColor(255, 255, 255, 180 * visiblePercent));

        for (Module m : modules) {
            m.onRender(context, startX + modulePadding, modulesStartY, width - (modulePadding * 2), 0, visiblePercent, mouseX, mouseY);
            modulesStartY += m.height + modulePadding;
        }

        int alpha = (int) ((150 + (20 * (1 - hoverPercent))) * visiblePercent);

        height = modulesStartY - startY + modulePadding;

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
        context.fill(
            startX + radius,
            startY,
            startX + width - radius,
            startY + radius,
            RGB.getColor(0, 0, 0, 50 * visiblePercent)
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

        context.getMatrices().popMatrix();

        super.onRender(context, startX, startY, width, height, visiblePercent, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverPercent = AnimHelper.handleAnimValue(hovered, hoverPercent * 100) / 100;
    }
}
