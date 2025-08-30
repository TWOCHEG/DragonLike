package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

public class BooleanSetArea extends RenderArea {
    private final Setting<Boolean> set;

    private float enableFactor = 0f;
    private final int buttonWidth = 15;
    private final int buttonHeight = textRenderer.fontHeight + padding;

    public BooleanSetArea(Setting<Boolean> set, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = set.getVisible() ? 1 : 0;
        this.set = set;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float showFa = showFactor * parentArea.showFactor;

        height += Render2D.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width - buttonWidth - padding * 2,
            padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * showFa)
        );

        Render2D.fill(
            context,
            startX + width - buttonWidth,
            startY + ((height - padding) / 2 - buttonHeight / 2),
            startX + width,
            startY + ((height - padding) / 2 - buttonHeight / 2) + buttonHeight,
            ColorUtils.fromRGB(100, 100, (int) (100 + (200 * enableFactor)), 200 * showFa),
            3, 2
        );
        int buttonX = (int) (startX + width - buttonWidth + 1 + (buttonWidth / 2 * enableFactor));
        Render2D.fill(
            context,
            buttonX,
            startY + ((height - padding) / 2 - buttonHeight / 2) + 1,
            (buttonX + buttonWidth / 2) - 1,
            startY + ((height - padding) / 2 - buttonHeight / 2) + buttonHeight - 1,
            ColorUtils.fromRGB(200, 200, 200, 200 * showFa),
            3, 2
        );

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
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
        enableFactor = AnimHelper.handle(set.getValue(), enableFactor);
        showFactor = AnimHelper.handle(set.getVisible(), showFactor);
    }
}
