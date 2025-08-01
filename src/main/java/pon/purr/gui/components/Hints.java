package pon.purr.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.purr.gui.ModulesGui;
import pon.purr.utils.RGB;
import pon.purr.utils.Render;
import pon.purr.utils.math.AnimHelper;


import java.util.*;

public class Hints extends RenderArea {
    private final LinkedList<String> hints = new LinkedList<>();
    private final ModulesGui gui;

    private boolean open = false;
    private float openPercent = 0f;

    private final int textPadding = 2;

    private final String closeText = "hints";

    public Hints(String text, ModulesGui gui) {
        super();
        this.gui = gui;
        for (String s : text.split("\n")) {
            hints.add(s);
        }
    }

    @Override
    public void render(DrawContext context, int startX, int startY, int width, int height, double mouseX, double mouseY) {
        int alpha = (int) (200 * gui.openPercent);

        String longestText;
        longestText = hints.get(0);
        for (String str : hints) {
            if (str.length() > longestText.length()) {
                longestText = str;
            }
        }

        int openHeight = textPadding + (textRenderer.fontHeight + textPadding) * hints.size();
        int openWidth = textRenderer.getWidth(longestText) + (textPadding * 2);

        int closeHeight = textPadding * 2 + textRenderer.fontHeight;
        int closeWidth = textRenderer.getWidth(closeText) + textPadding * 2;

        width = (int) (closeWidth + (openWidth - closeWidth) * openPercent);
        height = (int) (closeHeight + (openHeight - closeHeight) * openPercent);

        Render.fill(
            context,
            startX,
            startY - height,
            startX + width,
            startY,
            RGB.getColor(0, 0, 0, 150 * gui.openPercent),
            2,
            2
        );
        context.drawText(
            textRenderer,
            closeText,
            startX + textPadding,
            startY - (textRenderer.fontHeight + textPadding),
            RGB.getColor(255, 255, 255, alpha * (1 - openPercent)),
            false
        );
        if (openPercent > 0) {
            int text0 = startY - height + textPadding;
            int offSet = 0;
            for (String s : hints) {
                context.drawText(
                    textRenderer,
                    s,
                    startX + textPadding,
                    (int) (text0 + offSet * openPercent),
                    RGB.getColor(255, 255, 255, alpha * openPercent),
                    false
                );
                offSet += textRenderer.fontHeight + textPadding;
            }
        }


        super.render(context, startX, startY - height, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!open, openPercent);
    }
}
