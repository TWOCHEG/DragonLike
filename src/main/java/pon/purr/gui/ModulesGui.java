package pon.purr.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.purr.Purr;
import pon.purr.gui.components.*;
import pon.purr.modules.Parent;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.GetAnimDiff;
import pon.purr.utils.ColorUtils;
import pon.purr.utils.math.AnimHelper;
import pon.purr.managers.RotationManager;

import java.util.*;

public class ModulesGui extends Screen {
    private int frameCounter = 0;
    private final Gui gui;
    private final Screen previous;

    private boolean mouseDown = false;
    private int mouseDownCounter = 0;
    private float mouseDownPercent = 0f;
    private boolean mouseUp = false;
    private int mouseUpCounter = 0;
    private float mouseUpPercent = 0f;

    private float upPercent = 0f;
    private float downPercent = 0f;
    private float leftPercent = 0f;
    private float rightPercent = 0f;

    private boolean open = true;
    public float openPercent = 0f;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private float startY = 40;
    private float startX = 0;
    private final int categoriesShow;

    private float moveDelta = 0f;
    private double lastMouseX, lastMouseY = 0;

    private final HintsArea hints = new HintsArea(
        "RIGHT SHIFT - show keybinds\nMOUSE MIDDLE - bind module\n ⬅ ⬆ ⬇ ⮕ - move gui",
        this
    );

    public ModulesGui(Screen previous, Gui gui) {
        super(Text.literal("ModulesGui"));
        this.gui = gui;
        this.previous = previous;
        this.categoriesShow = 130 * this.gui.categories.size();
    }
    public void closeGui() {
        open = false;
        frameCounter = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        frameCounter++;

        animHandler();
        if (previous != null) previous.render(context, mouseX, mouseY, delta);

        LinkedList<CategoryArea> categories = gui.categories;

        int startX = (int) (((context.getScaledWindowWidth() / 2) - (((categoryWidth + categoryPadding) * categories.size()) / 2)) + this.startX);

        context.fillGradient(
            0, 0,
            this.width, this.height,
            ColorUtils.fromRGB(0, 0, 0, (int) (50 * openPercent)),
            ColorUtils.fromRGB(0, 0, 0, 0)
        );

        int closeCount = 0;
        for (int i = 0; i < categories.size(); i++) {
            CategoryArea c = categories.get(i);
            if (frameCounter > (categoriesShow / GetAnimDiff.get100X() / categories.size()) * i) {
                c.visibleReverse = !open;
            }

            if (c.visiblePercent == 0) closeCount++;

            c.render(context, startX, (int) startY, categoryWidth, 0, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }

        if (closeCount == categories.size() && !open && openPercent == 0) {
            client.setScreen(null);
        }
        hints.render(context, 5,  context.getScaledWindowHeight() - 5, 0, 0, mouseX, mouseY);

//        RenderPipeline renderPipeline = RenderPipeline.builder()
//            .withVertexFormat(VertexFormats.BLIT_SCREEN, VertexFormat.DrawMode.LINES)
//            .build();
//        if (!gui.image.getValue().equals("none")) {
//            context.drawTexture(
//                renderPipeline,
//                gui.texture,
//                gui.imageWidth,
//                gui.imageHeight,
//                0, 0,
//                context.getScaledWindowWidth() - gui.imageWidth,
//                context.getScaledWindowHeight() - gui.imageHeight,
//                gui.imageWidth,
//                gui.imageHeight
//            );
//        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!Parent.fullNullCheck()) {
            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            if (lastMouseX == 0 && lastMouseY == 0) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return;
            }

            float sensitivity = 0.05f;

            float yaw = client.player.getPitch() + (float) (deltaX * sensitivity);
            float pitch = Math.clamp(client.player.getYaw() + (float) (deltaY * sensitivity), -89.0f, 89.0f);
            Purr.rotations.rotate(yaw, pitch);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryArea c : gui.categories) {
            if (c.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeGui();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (RenderArea area : gui.categories) {
            if (area.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }

        if (scrollY < 0) {
            mouseUp = true;
            mouseUpCounter = frameCounter;
        } else {
            mouseDown = true;
            mouseDownCounter = frameCounter;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!open, openPercent);

        float moveDiff = GetAnimDiff.get() * 10;
        float moveAnimDiff = GetAnimDiff.get() * 10;
        float mouseDiff = GetAnimDiff.get() * 10;
        float mouseAnimDiff = GetAnimDiff.get() * 10;

        if (frameCounter > mouseUpCounter + GetAnimDiff.get100X() / 3 && mouseUp) {
            mouseUp = false;
        }
        if (frameCounter > mouseDownCounter + GetAnimDiff.get100X() / 3 && mouseDown) {
            mouseDown = false;
        }
        mouseDownPercent = AnimHelper.handleAnimValue(
            !mouseDown,
            mouseDownPercent,
            mouseAnimDiff
        );
        startY += mouseDiff * mouseDownPercent;
        mouseUpPercent = AnimHelper.handleAnimValue(
            !mouseUp,
            mouseUpPercent,
            mouseAnimDiff
        );
        startY -= mouseDiff * mouseUpPercent;

        upPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS),
            upPercent,
            moveAnimDiff
        );
        startY -= moveDiff * upPercent;
        downPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS),
            downPercent,
            moveAnimDiff
        );
        startY += moveDiff * downPercent;
        leftPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_RELEASE,
            leftPercent,
            moveAnimDiff
        );
        startX -= moveDiff * leftPercent;
        rightPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_RELEASE,
            rightPercent,
            moveAnimDiff
        );
        startX += moveDiff * rightPercent;

        moveDelta = AnimHelper.handleAnimValue(false, moveDelta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryArea c : gui.categories) {
            if (c.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (hints.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (CategoryArea c : gui.categories) {
            if (c.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderArea area : gui.categories) {
            if (area.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (RenderArea area : gui.categories) {
            if (area.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
