package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.GetAnimDiff;
import pon.main.utils.render.Render2D;

public class ButtonArea extends RenderArea {
    private Runnable onClick;
    private String name;

    private boolean clicked = false;
    private float clickedFactor = 0;

    public ButtonArea(RenderArea parentArea, Runnable onClick, String name) {
        super(parentArea);
        this.onClick = onClick;
        this.name = name;
    }

    @Override
    public void render(
            DrawContext context,
            int startX, int startY,
            int width, int height,
            double mouseX, double mouseY
    ) {
        width = mc.textRenderer.getWidth(name) + (padding * 2);
        height = mc.textRenderer.fontHeight + (padding * 2);
        Render2D.fill(
                context, startX, startY,
                startX + width,
                startY + height,
                ColorUtils.fromRGB(0, 0, 0, (100 + (30 * clickedFactor)) * parentArea.showFactor),
                bigPadding, 2
        );
        context.drawText(
                mc.textRenderer, name, startX + padding, startY + padding,
                ColorUtils.fromRGB(255, 255, 2555, (200 - (50 * clickedFactor)) * parentArea.showFactor),
                false
        );

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            clicked = true;
            onClick.run();
        }
        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (clicked) {
            clicked = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        clickedFactor = AnimHelper.handle(!clicked, clickedFactor, GetAnimDiff.get() * 4);
    }
}
