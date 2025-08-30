package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.modules.settings.Header;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

public class HeaderSetArea extends RenderArea {
    private final Header set;

    public HeaderSetArea(Header set, RenderArea parentArea) {
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
            width,
            padding,
            ColorUtils.fromRGB(200, 200, 200, 200 * showFa),
            true
        );

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && set.onClick != null) {
            set.onClick.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(set.getVisible(), showFactor);
    }
}
