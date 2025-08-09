package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.Color;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

public class BooleanSettingsArea extends RenderArea {
    private final Setting<Boolean> set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float enablePercent = 0f;
    private final int buttonWidth = 15;
    private final int buttonHeight = textRenderer.fontHeight + padding;

    private float showPercent = 0;

    public BooleanSettingsArea(Setting<Boolean> set, Object o) {
        super();
        this.set = set;
        if (o instanceof ModuleArea m) {
            this.module = m;
        } else if (o instanceof SettingsGroupArea g) {
            this.group = g;
        }
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float alphaPercent = showPercent * (
                module != null ?
                        module.openPercent * module.category.visiblePercent :
                        group.openPercent * group.module.category.visiblePercent
        );
        height += Render.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width - buttonWidth - padding * 2,
            padding,
            Color.fromRGB(255, 255, 255, 200 * alphaPercent)
        );

        Render.fill(
            context,
            startX + width - buttonWidth,
            startY + ((height - padding) / 2 - buttonHeight / 2),
            startX + width,
            startY + ((height - padding) / 2 - buttonHeight / 2) + buttonHeight,
            Color.fromRGB(100, 100, (int) (100 + (200 * enablePercent)), 200 * alphaPercent),
            3, 2
        );
        int buttonX = (int) (startX + width - buttonWidth + 1 + (buttonWidth / 2 * enablePercent));
        Render.fill(
            context,
            buttonX,
            startY + ((height - padding) / 2 - buttonHeight / 2) + 1,
            (buttonX + buttonWidth / 2) - 1,
            startY + ((height - padding) / 2 - buttonHeight / 2) + buttonHeight - 1,
            Color.fromRGB(200, 200, 200, 200 * alphaPercent),
            3, 2
        );

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            set.setValue(!set.getValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        enablePercent = AnimHelper.handleAnimValue(!set.getValue(), enablePercent);
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), showPercent);
    }
}
