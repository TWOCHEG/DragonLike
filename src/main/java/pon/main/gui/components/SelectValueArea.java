package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.gui.ModulesGui;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectValueArea extends RenderArea {
    public Object value;
    private Consumer<Object> task;
    private Supplier<Float> deltaProvider;
    private Supplier<Object> currentValueProvider;
    private Supplier<Object> oldValueProvider;
    private Function<Object, String> valueNameProvider = null;

    public SelectValueArea(
        Object value, RenderArea parentArea,
        Consumer<Object> task,
        Supplier<Float> deltaProvider,
        Supplier<Object> currentValueProvider,
        Supplier<Object> oldValueProvider,
        Function<Object, String> valueNameProvider
    ) {
        super(parentArea);
        this.value = value;
        this.task = task;
        this.deltaProvider = deltaProvider;
        this.currentValueProvider = currentValueProvider;
        this.oldValueProvider = oldValueProvider;
        this.valueNameProvider = valueNameProvider;
    }
    public SelectValueArea(
        Object value, RenderArea parentArea,
        Consumer<Object> task,
        Supplier<Float> deltaProvider,
        Supplier<Object> currentValueProvider,
        Supplier<Object> oldValueProvider
    ) {
        super(parentArea);
        this.value = value;
        this.task = task;
        this.deltaProvider = deltaProvider;
        this.currentValueProvider = currentValueProvider;
        this.oldValueProvider = oldValueProvider;
    }

    public float getShowFactor() {
        float showFactor = parentArea.showFactor;
        if (parentArea.parentArea != null) {
            showFactor *= parentArea.parentArea.showFactor;
        }
        return showFactor;
    }

    public String getValueName() {
        if (value instanceof Setting<?> set) {
            return set.getName();
        }
        if (valueNameProvider != null) {
            return valueNameProvider.apply(value);
        }
        if (parentArea.areas.contains(this)) {
            return "" + parentArea.areas.indexOf(this);
        }
        return "...";
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        String name = getValueName();
        float delta = deltaProvider.get();
        boolean isOldValue = value.equals(oldValueProvider.get());
        boolean isCurrentValue = value.equals(currentValueProvider.get());
        int startAlpha = 180;
        int alpha = (int) ((
                (startAlpha + (isOldValue || isCurrentValue ? (255 - startAlpha) * (isOldValue && !isCurrentValue ? 1 - delta : delta) : 0))
        ) * getShowFactor());

        context.drawText(
            mc.textRenderer, name,
            startX + padding,
            startY + padding,
            ColorUtils.fromRGB(255, 255, 255, alpha),
            false
        );
        width = mc.textRenderer.getWidth(name) + padding * 2;
        height = mc.textRenderer.fontHeight + padding * 2;

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            if (!Objects.equals(value, currentValueProvider.get())) {
                task.accept(value);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
