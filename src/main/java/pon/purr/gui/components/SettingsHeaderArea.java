package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.Setting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.Text;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;

public class SettingsHeaderArea extends RenderArea {
    private final Setting<Void> set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private final int textPadding = 2;

    private float showPercent = 0f;

    public SettingsHeaderArea(Setting<Void> set, ModuleArea module) {
        super();
        this.set = set;
        this.module = module;
    }
    public SettingsHeaderArea(Setting<Void> set, SettingsGroupArea group) {
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
        LinkedList<String> toDrawText = Text.splitForRender(set.getName(), width, textRenderer);
        for (String t : toDrawText) {
            context.drawText(
                textRenderer,
                t,
                startX + (width / 2 - textRenderer.getWidth(t) / 2),
                startY + height,
                RGB.getColor(200, 200, 200, 200 * showPercent),
                false
            );
            height += textRenderer.fontHeight + textPadding;
        }

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), module != null ? module.openPercent * module.category.visiblePercent : group.openPercent);
    }
}
