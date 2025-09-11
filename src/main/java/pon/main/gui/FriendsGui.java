package pon.main.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.events.impl.EventChangePlayerLook;
import pon.main.gui.components.FriendsWindowArea;
import pon.main.gui.components.RenderArea;
import pon.main.managers.Managers;
import pon.main.modules.Parent;
import pon.main.modules.client.Gui;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.ArrayList;
import java.util.List;

public class FriendsGui extends Screen {
    private final Gui gui;

    private boolean open = true;
    public float openFactor = 0f;

    public boolean dragged = false;

    private FriendsWindowArea fwa;

    public List<RenderArea> areas = new ArrayList<>();

    private double windowX = 100;
    private double windowY = 100;

    public FriendsGui() {
        super(Text.literal("friends"));
        this.gui = Managers.MODULE_MANAGER.getModule(Gui.class);
        this.fwa = gui.friendsWindowArea;
        areas.add(gui.choseGuiArea);
        areas.add(gui.friendsWindowArea);
    }

    @Override
    public void close() {
        open = false;
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
        if (openFactor == 0) client.setScreen(null);

        context.fillGradient(
            0, 0,
            this.width, this.height,
            ColorUtils.fromRGB(0, 0, 0, (int) (80 * gui.choseGuiArea.showFactor)),
            ColorUtils.fromRGB(0, 0, 0, 0)
        );

        gui.choseGuiArea.render(context, width / 2, 0, 0, 0, mouseX, mouseY);

        fwa.textRenderer = textRenderer;
        fwa.render(context, (int) windowX, (int) windowY, 0, 0, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (RenderArea area : areas) {
            if (area.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (RenderArea.checkHovered(fwa.x, fwa.y, fwa.width, fwa.titleHeight, mouseX, mouseY)) {
            dragged = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (RenderArea area : areas) {
            if (area.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == gui.getKeybind()) {
            gui.onDisable();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (RenderArea area : areas) {
            if (area.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        if (dragged) {
            windowX += deltaX;
            windowY += deltaY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (RenderArea area : areas) {
            if (area.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderArea area : areas) {
            if (area.mouseReleased(mouseX, mouseY, button)) return true;
        }
        if (dragged) {
            dragged = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void animHandler() {
        openFactor = AnimHelper.handle(open, openFactor, AnimHelper.AnimMode.EaseOut);
        fwa.show = open;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (RenderArea area : areas) {
            if (area.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }
        return true;
    }
}
