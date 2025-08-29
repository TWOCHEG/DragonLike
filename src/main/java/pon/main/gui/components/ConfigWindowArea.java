package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.gui.ConfigsGui;
import pon.main.modules.ui.Gui;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

public class ConfigWindowArea extends RenderArea {
    public boolean show = false;

    public int titleHeight = 17;

    public int radius = 7;

    private float draggedFactor = 0;

    private int windowHeight = 200;
    private int windowWidth = 400;

    private Gui gui;

    public ConfigWindowArea(Gui gui) {
        super();
        this.gui = gui;
        areas.add(new ButtonArea(
            this, () -> {
                gui.getConfig().openConfigDir();
            },
            "open in explorer"
        ));
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        startY -= (int) (50 * (1 - showFactor));

        Render2D.fill(
            context, startX, startY,
            startX + windowWidth,
            startY + windowHeight,
            CategoryArea.makeAColor(((150 + (30 * draggedFactor)) * showFactor) / 255),
            radius, 3
        );
        Render2D.fillPart(
            context, startX, startY,
            startX + windowWidth,
            startY + radius,
            ColorUtils.fromRGB(0, 0, 0, (50) * showFactor),
            3, true
        );
        context.fill(
            startX, startY + radius,
            startX + windowWidth, startY + radius + (titleHeight - radius),
            ColorUtils.fromRGB(0, 0, 0, 50 * showFactor)
        );

        areas.getFirst().render(
            context, startX + radius + padding, startY + ((titleHeight / 2) - (areas.getFirst().height / 2)),
            0, 0, mouseX, mouseY
        );

        context.drawCenteredTextWithShadow(
            textRenderer, "! (пока что) идут строительные работы !",
            startX + (windowWidth / 2), startY + ((windowHeight / 2) - (textRenderer.fontHeight / 2)),
            ColorUtils.fromRGB(255, 255, 255, 200 * showFactor)
        );

        super.render(context, startX, startY, windowWidth, windowHeight, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        if (mc.currentScreen instanceof ConfigsGui configsGui) {
            draggedFactor = AnimHelper.handle(!configsGui.dragged, draggedFactor);
        }
        showFactor = AnimHelper.handle(!show, showFactor, AnimHelper.AnimMode.EaseOut);
    }
}
