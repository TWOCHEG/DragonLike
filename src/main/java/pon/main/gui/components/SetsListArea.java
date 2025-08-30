package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.main.modules.settings.SetsList;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;
import pon.main.utils.math.AnimHelper;

public class SetsListArea extends RenderArea {
    public final SetsList set;

    private boolean open = false;
    private float openFactor = 0;

    private float delta = 0;

    private ListValue oldValue;

    public SetsListArea(SetsList set, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = set.getVisible() ? 1 : 0;
        this.set = set;

        for (Object v : set.getOptions()) {
            ListValue l = new ListValue(this, v);
            l.height = textRenderer.fontHeight + padding * 2;
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
        float showFa = showFactor * parentArea.showFactor;

        height += Render2D.drawTextWithTransfer(
            set.getName(),
            context,
            textRenderer,
            startX,
            startY,
            width,
            padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * showFa)
        );
        ListValue area = getValueArea();
        if (oldValue != null) {
            Render2D.fill(
                context,
                MathHelper.lerp(delta, oldValue.x, area.x),
                MathHelper.lerp(delta, oldValue.y, area.y),
                MathHelper.lerp(delta, oldValue.x + oldValue.width, area.x + area.width),
                MathHelper.lerp(delta, oldValue.y + oldValue.height, area.y + area.height),
                ColorUtils.fromRGB(0, 0, 0, 70 * showFa),
                vertexRadius,
                2
            );
        }
        height += area.height + padding;
        if (openFactor > 0) {
            int x = 0;
            int y = 0;
            for (RenderArea v : areas) {
                if (x + v.width > width) {
                    y += v.height + padding;
                    x = 0;
                }
                v.render(context, (int) (startX + x * openFactor), (int) (startY + height - v.height + y * openFactor), 0, 0, mouseX, mouseY);
                x += v.width + padding;
            }
            height += y * openFactor;
        } else {
            area.render(context, startX, startY + height - area.height, 0, 0, mouseX, mouseY);
        }

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
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
        if (openFactor > 0.9f) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public void animHandler() {
        delta = AnimHelper.handle(true, delta);
        showFactor = AnimHelper.handle(set.getVisible(), showFactor) ;
        openFactor = AnimHelper.handle(open, openFactor) * showFactor;
    }

    public class ListValue extends RenderArea {
        private final Object value;
        private final SetsListArea lst;
        public ListValue(SetsListArea lst, Object value) {
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
            float showFa = showFactor * lst.showFactor;

            boolean oldValue = this.equals(lst.oldValue);
            boolean currentValue = this.equals(lst.getValueArea());
            int color = 180;
            int colorDiff = 255 - color;
            int alpha = (int) ((
                (color + (oldValue || currentValue ? colorDiff * (oldValue && !currentValue ? 1 - lst.delta : lst.delta) * lst.openFactor : 0)) * (currentValue ? showFa : lst.openFactor)
            ) * showFa);
            context.drawText(
                textRenderer,
                value.toString(),
                startX + padding,
                startY + (this.height / 2 - textRenderer.fontHeight / 2),
                ColorUtils.fromRGB(255, 255, 255, alpha),
                false
            );
            width = textRenderer.getWidth(value.toString()) + padding * 2;

            super.render(context, startX, startY, width, this.height, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (checkHovered(mouseX, mouseY) && !this.equals(lst.getValueArea())) {
                lst.delta = 0;
                ListValue v = lst.getValueArea();
                if (!lst.oldValue.equals(v)) {
                    lst.oldValue = v;
                }
                lst.set.setValue(value);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
