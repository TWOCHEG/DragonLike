package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.SettingsGroup;
import pon.purr.utils.ColorUtils;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;


public class SettingsGroupArea extends RenderArea {
    private final SettingsGroup group;
    public final ModuleArea module;

    public float openPercent = 0f;

    private final int titleHeight = textRenderer.fontHeight + padding * 2;

    public SettingsGroupArea(SettingsGroup group, ModuleArea module) {
        super();
        this.group = group;
        this.module = module;

        this.areas = ModuleArea.getAreas(group.getOptions(), module, this);
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float alphaPercent = module.openPercent;
        for (RenderArea area : areas) {
            height += area.height + bigPadding;
        }
        height = (int) (titleHeight + height * openPercent);

        context.enableScissor(
            startX,
            startY,
            startX + width,
            startY + height
        );

        Render.fill(
            context,
            startX,
            startY,
            startX + width,
            startY + height,
            ColorUtils.fromRGB(100, 100, 100, (30 * alphaPercent) + (30 * openPercent)),
            5, 2
        );
        int titleX = startX + (width / 2 - textRenderer.getWidth(group.getName()) / 2);
        context.drawText(
            textRenderer,
            group.getName() + " " + (group.open ? "+" : "-"),
            titleX,
            startY + padding,
            ColorUtils.fromRGB(200, 200, 200, 200 * alphaPercent),
            false
        );

        int settingsY = startY + titleHeight + padding;
        for (RenderArea area : areas) {
            area.render(context, startX + padding, settingsY, width - padding * 2, 0, mouseX, mouseY);
            settingsY += area.height + bigPadding;
        }

        context.disableScissor();

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!(group.open && group.getVisible()), openPercent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(x, y, width, textRenderer.fontHeight + padding * 2, mouseX, mouseY) && module.openPercent > 0.9f) {
            group.open = !group.open;
            return true;
        }
        if (openPercent == 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
