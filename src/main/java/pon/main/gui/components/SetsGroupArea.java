package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.modules.settings.Group;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;


public class SetsGroupArea extends RenderArea {
    private final Group group;

    float visibleFactor;

    private final int titleHeight = textRenderer.fontHeight + padding * 2;

    public SetsGroupArea(Group group, RenderArea parentArea) {
        super(parentArea);
        this.group = group;
        this.visibleFactor = group.getVisible() ? 1 : 0;
        this.showFactor = 0;

        this.areas = ModuleArea.getAreas(group.getOptions(), this);
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float visibleFa = visibleFactor * parentArea.showFactor;

        for (RenderArea area : areas) {
            height += area.height + bigPadding;
        }
        height = (int) (titleHeight + height * showFactor);

        context.enableScissor(
            startX,
            startY,
            startX + width,
            startY + height
        );

        Render2D.fill(
            context,
            startX,
            startY,
            startX + width,
            startY + height,
            ColorUtils.fromRGB(100, 100, 100, (30 * visibleFa) + (30 * showFactor)),
            5, 2
        );
        int titleX = startX + (width / 2 - textRenderer.getWidth(group.getName()) / 2);
        context.drawText(
            textRenderer,
            group.getName() + " " + (group.open ? "+" : "-"),
            titleX,
            startY + padding,
            ColorUtils.fromRGB(200, 200, 200, 200 * visibleFa),
            false
        );

        int settingsY = startY + titleHeight + padding;
        for (RenderArea area : areas) {
            area.render(context, startX + padding, settingsY, width - padding * 2, 0, mouseX, mouseY);
            settingsY += area.height + bigPadding;
        }

        context.disableScissor();

        super.render(context, startX, startY, width, (int) (height * visibleFactor), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(group.open, showFactor);
        visibleFactor = AnimHelper.handle(group.getVisible(), visibleFactor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(x, y, width, textRenderer.fontHeight + padding * 2, mouseX, mouseY) && parentArea.showFactor > 0.9f) {
            group.open = !group.open;
            return true;
        }
        if (showFactor == 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
