package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.modules.settings.EnableableGroup;
import pon.main.modules.settings.Group;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;


public class SetsVGroupArea extends RenderArea {
    private final Setting group;

    private boolean open = false;

    private boolean enableSupport = false;
    private float enableFactor = 0;

    float visibleFactor;

    private final int titleHeight = textRenderer.fontHeight + padding * 2;

    private final int buttonWidth = 15;
    private final int buttonHeight = titleHeight - 2;

    public SetsVGroupArea(Group group, RenderArea parentArea) {
        super(parentArea);
        this.group = group;
        this.visibleFactor = group.getVisible() ? 1 : 0;
        this.showFactor = 0;

        this.areas = ModuleArea.getAreas(group.getOptions(), this);
    }
    public SetsVGroupArea(EnableableGroup group, RenderArea parentArea) {
        super(parentArea);
        this.group = group;
        this.visibleFactor = group.getVisible() ? 1 : 0;
        this.showFactor = 0;
        this.enableSupport = true;

        this.areas = ModuleArea.getAreas(group.settings, this);
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float lowShowFactor = visibleFactor * parentArea.showFactor;

        for (RenderArea area : areas) {
            if (area instanceof SetsVGroupArea sga) {
                height += (int) (area.height + (bigPadding * sga.visibleFactor));
            } else {
                height += (int) (area.height + (bigPadding * area.showFactor));
            }
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
            CategoryArea.makeAColor((50 + (20 * showFactor)) * lowShowFactor, 0.2f, true),
            5, 2
        );
        if (enableSupport) {
            Render2D.fill(
                context,
                startX + width - buttonWidth - padding,
                startY + 1,
                startX + width - padding,
                startY + buttonHeight,
                ColorUtils.fromRGB(100, 100, (int) (100 + (200 * enableFactor)), 100 * lowShowFactor),
                3, 2
            );
            int buttonX = (int) (startX + width - buttonWidth - padding + 1 + (buttonWidth / 2 * enableFactor));
            Render2D.fill(
                context,
                buttonX,
                startY + 2,
                (buttonX + buttonWidth / 2) - 1,
                startY + buttonHeight - 1,
                ColorUtils.fromRGB(200, 200, 200, 100 * lowShowFactor),
                3, 2
            );
        }
        int titleX = enableSupport ? startX + padding : startX + (width / 2 - textRenderer.getWidth(group.getName()) / 2);
        context.drawText(
            textRenderer,
            group.getName() + " " + (open ? "+" : "-"),
            titleX,
            startY + padding,
            ColorUtils.fromRGB(200, 200, 200, 200 * lowShowFactor),
            false
        );

        int settingsY = startY + titleHeight + padding;
        for (RenderArea area : areas) {
            area.render(context, startX + padding, settingsY, width - padding * 2, 0, mouseX, mouseY);
            if (area instanceof SetsVGroupArea sga) {
                settingsY += (int) (area.height + (bigPadding * sga.visibleFactor));
            } else {
                settingsY += (int) (area.height + (bigPadding * area.showFactor));
            }
        }

        context.disableScissor();

        super.render(context, startX, startY, width, (int) (height * visibleFactor), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        if (group instanceof Group g) {
            open = g.open;
        } else if (group instanceof EnableableGroup eg) {
            open = eg.open;
        }
        if (enableSupport) {
            enableFactor = AnimHelper.handle((boolean) group.getValue(), enableFactor);
        }
        showFactor = AnimHelper.handle(open && group.getVisible(), showFactor);
        visibleFactor = AnimHelper.handle(group.getVisible(), visibleFactor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(x + width - buttonWidth, y, buttonWidth, buttonHeight, mouseX, mouseY) && parentArea.showFactor > 0.9f && group instanceof EnableableGroup eg) {
            eg.setValue(!eg.getValue());
            return true;
        } else if (checkHovered(x, y, width, textRenderer.fontHeight + padding * 2, mouseX, mouseY) && parentArea.showFactor > 0.9f) {
            if (group instanceof Group g) {
                g.open = !g.open;
            } else if (group instanceof EnableableGroup eg) {
                eg.open = !eg.open;
            }
            return true;
        }
        if (showFactor == 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
