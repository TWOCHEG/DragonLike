package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.purr.modules.settings.ListSetting;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.Text;
import pon.purr.utils.math.AnimHelper;
import pon.purr.utils.math.MathUtils;

import java.util.*;

public class ListSettingsArea extends RenderArea {
    public final ListSetting set;

    private ModuleArea module = null;
    private SettingsGroupArea group = null;

    private float showPercent = 0f;

    private boolean open = false;
    private float openPercent = 0f;

    private int saveHeight = 0;

    private float delta = 0f;

    private ListValue oldValue;

    public ListSettingsArea(ListSetting set, Object o) {
        super();
        this.set = set;
        if (o instanceof ModuleArea m) {
            this.module = m;
        } else if (o instanceof SettingsGroupArea g) {
            this.group = g;
        }

        for (Object v : set.getOptions()) {
            ListValue l = new ListValue(this, v);
            l.height = module != null ?
                    module.textRenderer.fontHeight + module.padding * 2 :
                    group.module.textRenderer.fontHeight + group.module.padding * 2;
            areas.add(l);
        }
        this.oldValue = getValueArea();
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
            RGB.getColor(255, 255, 255, 200 * showPercent)
        );
        ListValue area = getValueArea();
        if (oldValue != null) {
            Render.fill(
                context,
                MathHelper.lerp(delta, oldValue.x, area.x),
                MathHelper.lerp(delta, oldValue.y, area.y),
                MathHelper.lerp(delta, oldValue.x + oldValue.width, area.x + area.width),
                MathHelper.lerp(delta, oldValue.y + oldValue.height, area.y + area.height),
                RGB.getColor(0, 0, 0, 70 * showPercent),
                vertexRadius,
                2
            );
        }
        height += area.height;
        if (openPercent > 0) {
            int x = 0;
            int y = 0;
            for (RenderArea v : areas) {
                if (x + v.width > width) {
                    y += v.height + padding;
                    x = 0;
                }
                v.render(context, (int) (startX + x * openPercent), (int) (startY + height - v.height + y * openPercent), 0, 0, mouseX, mouseY);
                x += v.width + padding;
            }
            height += y * openPercent;
        } else {
            area.render(context, startX, startY + height - area.height, 0, 0, mouseX, mouseY);
        }

        super.render(context, startX, startY, width, (int) (height * showPercent), mouseX, mouseY);
    }

    private ListValue getValueArea() {
        for (RenderArea area : areas) {
            if (area instanceof ListValue l) {
                if (l.value.equals(set.getValue())) {
                    return l;
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !open) {
                int i = set.getOptions().indexOf(set.getValue()) + 1;
                if (i + 1 > set.getOptions().size()) i = 0;
                delta = 0;
                if (!oldValue.equals(getValueArea())) {
                    oldValue = getValueArea();
                }
                set.setValue(set.getOptions().get(i));
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                open = !open;
            }
        }
        if (openPercent > 0.9f) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public void animHandler() {
        delta = AnimHelper.handleAnimValue(false, delta);
        showPercent = AnimHelper.handleAnimValue(!set.getVisible(), module != null ? module.openPercent * module.category.visiblePercent : group.openPercent);
        openPercent = AnimHelper.handleAnimValue(!open, openPercent) * showPercent;
    }

    public class ListValue extends RenderArea {
        private final Object value;
        private final ListSettingsArea lst;
        public ListValue(ListSettingsArea lst, Object value) {
            this.lst = lst;
            this.value = value;
        }

        @Override
        public void render(
            DrawContext context,
            int startX, int startY,
            int width, int height,
            double mouseX, double mouseY
        ) {
            boolean oldValue = this.equals(lst.oldValue);
            boolean currentValue = this.equals(lst.getValueArea());
            int color = 180;
            int colorDiff = 255 - color;
            int alpha = (int) (
                (color + (oldValue || currentValue ? colorDiff * (oldValue ? 1 - lst.delta : lst.delta) * lst.openPercent : 0)) * (currentValue ? lst.showPercent : lst.openPercent)
            );
            context.drawText(
                textRenderer,
                value.toString(),
                startX + padding,
                startY + padding ,
                RGB.getColor(255, 255, 255, alpha),
                false
            );
            width = textRenderer.getWidth(value.toString()) + padding * 2;
            height = textRenderer.fontHeight + padding * 2;

            super.render(context, startX, startY, width, height, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (checkHovered(mouseX, mouseY)) {
                lst.delta = 0;
                if (!lst.oldValue.equals(getValueArea())) {
                    lst.oldValue = getValueArea();
                }
                lst.set.setValue(value);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
