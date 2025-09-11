package pon.main.modules.hud.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.gui.components.CategoryArea;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.FrameRateCounter;
import pon.main.utils.render.Render2D;

public class FPSHud extends HudArea {
    public FPSHud() {
        super();
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        String text = "fps " + FrameRateCounter.INSTANCE.getFps();

        width = mc.textRenderer.getWidth(text) + (padding * 2);

        height = mc.textRenderer.fontHeight + (padding * 2);

        Render2D.fill(
            context,
            x, y, x + width, y + height,
            CategoryArea.makeAColor((100 + (30 * draggedFactor)) * showFactor),
            bigPadding, 2
        );
        context.drawText(
            mc.textRenderer, text,
            x + padding,
            y + padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
            false
        );

        super.render(context, x, y, width, height, mouseX, mouseY);
    }
}
