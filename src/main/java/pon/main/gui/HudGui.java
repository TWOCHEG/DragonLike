package pon.main.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.main.Main;
import pon.main.gui.components.ButtonArea;
import pon.main.gui.components.ContextMenu;
import pon.main.gui.components.ModuleArea;
import pon.main.gui.components.RenderArea;
import pon.main.managers.Managers;
import pon.main.modules.client.Gui;
import pon.main.modules.hud.HudModule;
import pon.main.modules.hud.components.HudArea;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;

public class HudGui extends Screen {
    private final Gui gui;
    private final HudModule hud;

    private boolean open = true;
    public float openFactor = 0f;

    private ContextMenu cm;

    public void setCM(ContextMenu cm) {
        this.cm = cm;
    }
    public void resetCM() {
        this.cm = null;
    }

    public HudGui() {
        super(Text.literal("hud"));
        this.gui = Managers.MODULE_MANAGER.getModule(Gui.class);
        this.hud = Managers.MODULE_MANAGER.getModule(HudModule.class);
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

        context.fillGradient(
            0, 0,
            this.width, this.height,
            ColorUtils.fromRGB(0, 0, 0, (int) (80 * gui.choseGuiArea.showFactor)),
            ColorUtils.fromRGB(0, 0, 0, 0)
        );
        gui.choseGuiArea.render(context, width / 2, 0, 0, 0, mouseX, mouseY);

        hud.render(context);

        if (cm != null) {
            cm.render(context, -1, -1, 80, -1, mouseX, mouseY);
        }
    }

    private void animHandler() {
        openFactor = AnimHelper.handle(open, openFactor, AnimHelper.AnimMode.EaseOut);
        if (openFactor == 0 && !open) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gui.choseGuiArea.mouseClicked(mouseX, mouseY, button)) return true;
        if (cm != null && cm.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            List<RenderArea> list = new LinkedList<>();
            for (HudArea area : Managers.HUD_MANAGER.hudList()) {
                if (area.checkHovered(mouseX, mouseY)) {
                    list.addAll(ModuleArea.getAreas(area.getSettings(), null));

                    list.add(
                        new ButtonArea.Builder("🗑 delete")
                            .onClick(() -> {
                                area.setEnable(false);
                            })
                            .build()
                    );
                    break;
                }
            }

            if (list.isEmpty()) {
                for (HudArea area : Managers.HUD_MANAGER.hudList()) {
                    if (area.getEnable()) {
                        ButtonArea buttonArea = new ButtonArea.Builder("- " + area.getName())
                            .onClick(() -> {
                                area.setEnable(false);
                            })
                            .color(ColorUtils.fromRGB(255, 220, 200))
                            .build();
                        list.add(buttonArea);
                    } else {
                        ButtonArea buttonArea = new ButtonArea.Builder("+ " + area.getName())
                            .onClick(area::onEnableInGui)
                            .build();

                        list.add(buttonArea);
                    }
                }
            }
            if (!list.isEmpty()) {
                setCM(
                    new ContextMenu.Builder()
                        .areas(list)
                        .position(new double[]{mouseX, mouseY})
                        .closeTask(this::resetCM)
                        .showFactorProvider(() -> openFactor)
                        .build()
                );
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == gui.getKeybind()) {
            gui.onDisable();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
