package pon.main.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.EventChangePlayerLook;
import pon.main.gui.components.*;
import pon.main.modules.Parent;
import pon.main.modules.client.Gui;
import pon.main.utils.math.GetAnimDiff;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.Timer;

import java.util.LinkedList;

public class ModulesGui extends Screen {
    private final Gui gui;

    private float scrollVelocityY = 0f;
    private float moveVelocityX = 0f;
    private float moveVelocityY = 0f;

    private static final float SCROLL_FRICTION = 0.92f;
    private static final float MOVE_FRICTION = 0.85f;
    private static final float MOVE_ACCELERATION = 1.5f;
    private static final float SCROLL_SENSITIVITY = 5f;

    private float upFactor = 0f;
    private float downFactor = 0f;
    private float leftFactor = 0f;
    private float rightFactor = 0f;

    private boolean open = true;
    public float openFactor;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private float startY = 40;
    private float startX = 0;

    private Timer timer = new Timer();

    private final HintsArea hints = new HintsArea(
        "RIGHT SHIFT - show keybinds\nMOUSE MIDDLE - bind module\n ⬅ ⬆ ⬇ ⮕ - move gui",
        this
    );

    public ModulesGui() {
        super(Text.literal("modules"));
        this.gui = Main.MODULE_MANAGER.getModule(Gui.class);
    }

    @Override
    public void close() {
        open = false;
        timer.reset();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (client.world == null) {
            this.renderPanoramaBackground(context, deltaTicks);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animHandler();

        LinkedList<CategoryArea> categories = gui.categories;

        int startX = (int) (((width / 2) - (((categoryWidth + categoryPadding) * categories.size()) / 2)) + this.startX);

        context.fillGradient(
            0, 0,
            this.width, this.height,
            ColorUtils.fromRGB(0, 0, 0, (int) (80 * gui.choseGuiArea.showFactor)),
            ColorUtils.fromRGB(0, 0, 0, 0)
        );

        gui.choseGuiArea.render(context, width / 2, 0, 0, 0, mouseX, mouseY);

        if (gui.texture != null) {
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                gui.texture,
                width - gui.imageWidth,
                (int) (height - (gui.imageHeight * openFactor)),
                0, 0,
                gui.imageWidth, gui.imageHeight,
                gui.imageWidth, gui.imageHeight,
                ColorUtils.fromRGB(255, 255, 255, 255 * openFactor)
            );
        }

        int closeCount = 0;
        for (int i = 0; i < categories.size(); i++) {
            CategoryArea c = categories.get(i);
            if (timer.getTimeMs() > ((100L * this.gui.categories.size()) / categories.size()) * i) {
                c.show = open;
            }

            if (c.showFactor == 0) closeCount++;

            c.render(context, startX, (int) startY, categoryWidth, 0, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }

        if (closeCount == categories.size() && !open && openFactor == 0) {
            client.setScreen(null);
        }
        hints.render(context, 5,  context.getScaledWindowHeight() - 5, 0, 0, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryArea c : gui.categories) {
            if (c.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            gui.onDisable();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (RenderArea area : gui.categories) {
            if (area.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }
        scrollVelocityY += (float) scrollY * SCROLL_SENSITIVITY;
        return true;
    }

    private void animHandler() {
        openFactor = AnimHelper.handle(open, openFactor, AnimHelper.AnimMode.EaseOut);

        float moveDiff = GetAnimDiff.get() * 2;
        float moveAnimDiff = GetAnimDiff.get();

        startY += scrollVelocityY;
        scrollVelocityY *= SCROLL_FRICTION;
        if (Math.abs(scrollVelocityY) < 0.1f) {
            scrollVelocityY = 0f;
        }

        boolean upPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
        boolean downPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
        boolean leftPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
        boolean rightPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;

        upFactor = AnimHelper.handle(upPressed, upFactor, moveAnimDiff, AnimHelper.AnimMode.EaseOut);
        downFactor = AnimHelper.handle(downPressed, downFactor, moveAnimDiff, AnimHelper.AnimMode.EaseOut);
        leftFactor = AnimHelper.handle(leftPressed, leftFactor, moveAnimDiff, AnimHelper.AnimMode.EaseOut);
        rightFactor = AnimHelper.handle(rightPressed, rightFactor, moveAnimDiff, AnimHelper.AnimMode.EaseOut);

        float moveDirY = (downFactor - upFactor);
        float moveDirX = (rightFactor - leftFactor);

        moveVelocityY = moveVelocityY * MOVE_FRICTION + moveDirY * MOVE_ACCELERATION;
        moveVelocityX = moveVelocityX * MOVE_FRICTION + moveDirX * MOVE_ACCELERATION;

        startY += moveVelocityY * moveDiff;
        startX += moveVelocityX * moveDiff;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gui.choseGuiArea.mouseClicked(mouseX, mouseY, button)) return true;
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