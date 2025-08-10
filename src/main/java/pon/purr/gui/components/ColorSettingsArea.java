package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.modules.settings.ColorSettings;
import pon.purr.utils.ColorUtils;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;

public class ColorSettingsArea extends RenderArea {
    private final ColorSettings set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float showPercent = 0f;


    public ColorSettingsArea(ColorSettings set, Object o) {
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
            "карта цветов покачто в разработке",
            context, textRenderer,
            startX, startY, width, padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * alphaPercent)
        );

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), showPercent);
    }
}
