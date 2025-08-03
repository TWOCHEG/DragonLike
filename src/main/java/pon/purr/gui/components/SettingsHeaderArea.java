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

    private float showPercent = 0f;

    public SettingsHeaderArea(Setting<Void> set, Object o) {
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
        height += Render.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width,
            padding,
            RGB.getColor(200, 200, 200, 200 * showPercent),
            true
        );

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), module != null ? module.openPercent * module.category.visiblePercent : group.openPercent);
    }
}
