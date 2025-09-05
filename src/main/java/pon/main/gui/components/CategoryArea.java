package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.Main;
import pon.main.Main.Categories;
import pon.main.modules.Parent;
import pon.main.modules.settings.ColorSet;
import pon.main.modules.client.Gui;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

import java.util.*;

public class CategoryArea extends RenderArea {
    private final Categories name;

    private float hoverFactor = 0f;
    private boolean hovered = false;

    public boolean show = false;

    public CategoryArea(LinkedList<Parent> modules, Categories name) {
        super();
        this.showFactor = 0;
        modules.forEach(m -> {
            areas.add(new ModuleArea(m, this).setParentArea(this));
        });
        this.name = name;
    }

    public static int makeAColor(float alpha, float darkness, boolean highlight) {
        float diff;
        if (highlight) {
            diff = 255 * darkness;
        } else {
            diff = -(255 * darkness);
        }
        ColorSet c = Main.MODULE_MANAGER.getModule(Gui.class).theme;
        return ColorUtils.fromRGB(
            (int) (c.r() + diff),
            (int) (c.g() + diff),
            (int) (c.b() + diff),
            c.a() * (alpha / 255)
        );
    }
    public static int makeAColor(float alpha, float darkness) {
        return makeAColor(alpha, darkness, false);
    }
    public static int makeAColor(float alpha) {
        return makeAColor(alpha, 0, false);
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        float offset = 100 * (1 - showFactor);

        startY -= offset;

        int modulesStartY = startY + 26;
        int modulePadding = 2;

        for (RenderArea m : areas) {
            height += m.height + modulePadding;
        }
        height += (modulesStartY - startY) + modulePadding;

        hovered = checkHovered(mouseX, mouseY);
        int radius = 7;

        Render2D.fill(
            context,
            startX, startY,
            startX + width,
            (startY + height),
            makeAColor((150 + (10 * hoverFactor)) * showFactor),
            radius, 3
        );
        Render2D.fillPart(
            context,
            startX,
            startY,
            startX + width,
            startY + radius,
            ColorUtils.fromRGB(0, 0, 0, 50 * showFactor),
            3, true
        );
        context.fillGradient(
            startX,
            (startY + radius),
            startX + width,
            startY + height - radius,
            ColorUtils.fromRGB(0, 0, 0, 50 * showFactor),
            ColorUtils.fromRGB(0, 0, 0, 0)
        );
        int nameStartY = (startY + 6);
        int nameContainerPadding = 3;
        Render2D.fill(
            context,
            startX - nameContainerPadding + (width / 2 - textRenderer.getWidth(name.name()) / 2),
            nameStartY - nameContainerPadding,
            startX + nameContainerPadding + (width / 2 + textRenderer.getWidth(name.name()) / 2),
            nameStartY + textRenderer.fontHeight + nameContainerPadding,
            makeAColor(100 * showFactor, 0.25f),
            5, 2
        );
        context.drawText(
            textRenderer,
            name.name(),
            startX + (width / 2 - textRenderer.getWidth(name.name()) / 2),
            nameStartY,
            ColorUtils.fromRGB(255, 255, 255, 180 * showFactor),
            false
        );
        context.drawHorizontalLine(startX + nameContainerPadding + 12, startX + width - 20, (modulesStartY - 5), ColorUtils.fromRGB(255, 255, 255, 50 * showFactor));

        for (RenderArea m : areas) {
            m.render(context, startX + modulePadding, modulesStartY, width - (modulePadding * 2), 0, mouseX, mouseY);
            modulesStartY += m.height + modulePadding;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        hoverFactor = AnimHelper.handle(hovered, hoverFactor);
        showFactor = AnimHelper.handle(show, showFactor, AnimHelper.AnimMode.EaseOut);
    }
}
