package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.Text;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;

public class BooleanSettingsArea extends RenderArea {
    private final Setting<Boolean> set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float enablePercent = 0f;
    private final int textPadding = 2;

    private final int buttonWidth = 15;
    private final int buttonHeight = textRenderer.fontHeight + textPadding;

    private float showPercent = 0f;

    public BooleanSettingsArea(Setting<Boolean> set, ModuleArea module) {
        super();
        this.set = set;
        this.module = module;
    }
    public BooleanSettingsArea(Setting<Boolean> set, SettingsGroupArea group) {
        super();
        this.set = set;
        this.group = group;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
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

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY) && showPercent > 0.9f) {
            set.setValue(!set.getValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        enablePercent = AnimHelper.handleAnimValue(!set.getValue(), enablePercent);
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), module != null ? module.openPercent * module.category.visiblePercent : group.openPercent);
    }
}
