package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.Text;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;

public class BooleanSet extends RenderArea {
    private final Setting<Boolean> set;

    private final Module module;

    private float enablePercent = 0f;
    private final int textPadding = 2;

    private final int buttonWidth = 15;
    private final int buttonHeight = textRenderer.fontHeight + textPadding;

    public BooleanSet(Setting<Boolean> set, Module module) {
        super();
        this.set = set;
        this.module = module;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float showPercent = module.openPercent * module.category.visiblePercent;

        LinkedList<String> toDrawText = Text.splitForRender(set.getName(), width - buttonWidth - textPadding, textRenderer);
        int textY = startY;
        for (String t : toDrawText) {
            context.drawText(
                textRenderer,
                t.strip(),
                startX,
                textY,
                RGB.getColor(255, 255, 255, 200 * showPercent),
                false
            );
            textY += textRenderer.fontHeight + textPadding;
            height += textRenderer.fontHeight + textPadding;
        }

        Render.fill(
            context,
            startX + width - buttonWidth,
            startY + (height / 2 - buttonHeight / 2),
            startX + width,
            startY + (height / 2 - buttonHeight / 2) + buttonHeight,
            RGB.getColor(100, 100, (int) (100 + (200 * enablePercent)), 200 * showPercent),
            3, 2
        );
        int buttonX = (int) (startX + width - buttonWidth + 1 + (buttonWidth / 2 * enablePercent));
        Render.fill(
            context,
            buttonX,
            startY + (height / 2 - buttonHeight / 2) + 1,
            (buttonX + buttonWidth / 2) - 1,
            startY + (height / 2 - buttonHeight / 2) + buttonHeight - 1,
            RGB.getColor(200, 200, 200, 200 * showPercent),
            3, 2
        );
        height *= showPercent;

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && module.openPercent > 0.9f) {
            set.setValue(!set.getValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        enablePercent = AnimHelper.handleAnimValue(!set.getValue(), enablePercent);
    }
}
