package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.Group;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;


public class GroupArea extends RenderArea {
    private final Group group;
    private final Module module;

    public float openPercent = 0f;

    private final int padding = 2;
    private final int titleHeight = textRenderer.fontHeight + padding * 2;

    public GroupArea(Group group, Module module) {
        super();
        this.group = group;
        this.module = module;

        this.areas = Module.getAreas(group.getOptions(), this);
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        openPercent = openPercent * module.openPercent * module.category.visiblePercent;
        float alphaPercent = module.openPercent * module.category.visiblePercent;
        for (RenderArea area : areas) {
            height += area.height + padding * 2;
        }
        height = (int) (titleHeight + height * openPercent);

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            startY + height,
            RGB.getColor(100, 100, 100, (30 * alphaPercent) + (30 * openPercent)),
            5, 2
        );
        int titleX = startX + (width / 2 - textRenderer.getWidth(group.getName()) / 2);
        context.drawText(
            textRenderer,
            group.getName(),
            titleX,
            startY + padding,
            RGB.getColor(200, 200, 200, 200 * alphaPercent),
            false
        );
        if (openPercent > 0) {
            context.drawHorizontalLine(
                (int) (startX + (width / 2) - (textRenderer.getWidth(group.getName()) / 2) * openPercent),
                (int) (startX + (width / 2) + (textRenderer.getWidth(group.getName()) / 2) * openPercent),
                startY + titleHeight - padding,
                RGB.getColor(200, 200, 200, 200 * openPercent)
            );
        }

        int settingsY = startY + titleHeight + padding;
        for (RenderArea area : areas) {
            area.render(context, startX + padding, settingsY, width - padding * 2, 0, mouseX, mouseY);
            settingsY += area.height + padding;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!group.open, openPercent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(x, y, width, textRenderer.fontHeight + padding * 2, mouseX, mouseY) && module.openPercent > 0.9f) {
            group.open = !group.open;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
