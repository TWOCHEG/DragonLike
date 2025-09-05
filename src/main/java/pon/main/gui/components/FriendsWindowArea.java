package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pon.main.gui.FriendsGui;
import pon.main.managers.FriendsManager;
import pon.main.managers.Managers;
import pon.main.utils.ColorUtils;
import pon.main.utils.TextUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.math.Timer;
import pon.main.utils.render.Render2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class FriendsWindowArea extends RenderArea {
    public boolean show = false;

    public final int titleHeight = 17;

    public final int radius = 7;

    private float draggedFactor = 0;

    private final int windowHeight = 300;
    private final int windowWidth = 170;

    private float friendsDrawY = titleHeight + bigPadding;

    private float scrollVelocityY = 0f;
    private static final float SCROLL_FRICTION = 0.7f;
    private static final float SCROLL_SENSITIVITY = 5f;
    private float targetFriendsDrawY = 0;
    private boolean isScrollingToTarget = false;
    private static final float SCROLL_ANIM_SPEED = 0.1f;

    private float bounceBackFactor = 0f;

    private ContextMenu cm = null;

    private final ArrayList<RenderArea> toDelete = new ArrayList<>();

    public void resetCM() {
        if (cm != null) {
            areas.remove(cm);
            this.cm = null;
        }
    }
    public void setCM(ContextMenu cm) {
        if (this.cm != null) areas.remove(this.cm);
        this.cm = cm;
        areas.addFirst(cm);
    }

    public void deleteFriendArea(FriendArea fa) {
        toDelete.add(fa);
    }

    public void addFriendArea(FriendArea fa) {
        areas.add(fa);
    }

    public FriendsWindowArea() {
        super();

        for (String path : FriendsManager.friends) {
            areas.add(new FriendArea(
                this, path
            ));
        }
    }

    public FriendArea getFriendArea(String name) {
        for (RenderArea area : areas) {
            if (area instanceof FriendArea friendArea) {
                if (friendArea.name.equals(name)) return friendArea;
            }
        }
        return null;
    }

    private float calculateTotalContentHeight() {
        float totalHeight = 0;
        for (RenderArea area : areas) {
            if (area instanceof FriendArea fa) {
                totalHeight += fa.height + (padding * fa.showFactor);
            }
        }
        return totalHeight;
    }

    public void changeFriendsY(float factor) {
        float totalHeight = calculateTotalContentHeight();
        float availableHeight = windowHeight - titleHeight - 2 * bigPadding;
        float maxScroll = titleHeight + bigPadding;
        float minScroll = maxScroll - Math.max(0, totalHeight - availableHeight);

        targetFriendsDrawY = maxScroll - factor * (maxScroll - minScroll);
        targetFriendsDrawY = MathHelper.clamp(targetFriendsDrawY, minScroll, maxScroll);

        isScrollingToTarget = true;
        scrollVelocityY = 0;
        bounceBackFactor = 0;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        height = windowHeight;
        width = windowWidth;

        startY -= (int) (50 * (1 - showFactor));

        Render2D.fill(
            context, startX, startY,
            startX + width, startY + height,
            CategoryArea.makeAColor((150 + (30 * draggedFactor)) * showFactor),
            radius, 3
        );

        float textVFactor = !areas.isEmpty() && areas.getLast() instanceof FriendArea ? 1 - areas.getLast().showFactor : 1;
        if (textVFactor != 0) {
            String t = "у тебя нет друзей :)";
            int eHeight = ((textRenderer.fontHeight + padding) * TextUtils.splitForRender(
                t, width - (bigPadding * 2), textRenderer::getWidth
            ).size()) - padding;
            Render2D.drawTextWithTransfer(
                t, context, textRenderer,
                startX + ((width / 2) - ((width - (bigPadding * 2)) / 2)),
                startY + ((height / 2) - (eHeight / 2)),
                width - (bigPadding * 2), padding,
                ColorUtils.fromRGB(200, 200, 200, (130 * textVFactor) * showFactor),
                true
            );
        }

        Render2D.fillPart(
            context, startX, startY,
            startX + windowWidth,
            startY + radius,
            ColorUtils.fromRGB(0, 0, 0, (50) * showFactor),
            3, true
        );
        context.fill(
            startX, startY + radius,
            startX + windowWidth, startY + radius + (titleHeight - radius),
            ColorUtils.fromRGB(0, 0, 0, 50 * showFactor)
        );

        String name = "friends window";
        context.drawText(
            textRenderer, name,
            (startX + width - bigPadding) - textRenderer.getWidth(name),
            startY + ((titleHeight / 2) - (textRenderer.fontHeight / 2)),
            ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
            false
        );

        context.enableScissor(
            startX, startY + titleHeight, startX + width, startY + height - 1
        );

        int friendsY = (int) (startY + friendsDrawY);
        for (RenderArea area : areas) {
            if (area instanceof ContextMenu) continue;
            area.render(context, startX + bigPadding, friendsY, width - (bigPadding * 2), 30, mouseX, mouseY);
            friendsY += (int) (area.height + (padding * area.showFactor));
        }
        areas.removeAll(toDelete);
        toDelete.clear();

        context.disableScissor();

        if (cm != null) {
            cm.render(context, -1, -1, 80, -1, mouseX, mouseY);
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (checkHovered(mouseX, mouseY)) {
            scrollVelocityY += (float) scrollY * SCROLL_SENSITIVITY;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void animHandler() {
        if (mc.currentScreen instanceof FriendsGui friendsGui) {
            draggedFactor = AnimHelper.handle(friendsGui.dragged, draggedFactor);
        }
        showFactor = AnimHelper.handle(show, showFactor, AnimHelper.AnimMode.EaseOut);

        if (isScrollingToTarget) {
            float diff = targetFriendsDrawY - friendsDrawY;

            if (Math.abs(diff) < 0.5f) {
                friendsDrawY = targetFriendsDrawY;
                isScrollingToTarget = false;
            }
            else {
                friendsDrawY += diff * SCROLL_ANIM_SPEED;
            }
        }

        friendsDrawY += scrollVelocityY;
        scrollVelocityY *= SCROLL_FRICTION;

        if (Math.abs(scrollVelocityY) < 0.1f) {
            scrollVelocityY = 0f;
        }

        float totalHeight = calculateTotalContentHeight();
        float availableHeight = windowHeight - titleHeight - 2 * bigPadding;
        float maxScroll = titleHeight + bigPadding;
        float minScroll = maxScroll - Math.max(0, totalHeight - availableHeight);

        if (friendsDrawY > maxScroll) {
            float overshoot = friendsDrawY - maxScroll;
            bounceBackFactor = AnimHelper.handle(true, bounceBackFactor);
            friendsDrawY = maxScroll + overshoot * (1 - bounceBackFactor);
        }
        else if (friendsDrawY < minScroll) {
            float overshoot = minScroll - friendsDrawY;
            bounceBackFactor = AnimHelper.handle(true, bounceBackFactor);
            friendsDrawY = minScroll - overshoot * (1 - bounceBackFactor);
        }
        else {
            bounceBackFactor = 0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setCM(new ContextMenu(
                this, this, new double[] {mouseX, mouseY},
                new RenderArea[] {
                    new ButtonInputArea(
                        this, (s) -> {
                            if (!FriendsManager.friends.contains(s)) {
                                Managers.FRIENDS.addFriend(s);
                                addFriendArea(new FriendArea(
                                    this, s
                                ));
                            } else {
                                FriendArea fa = getFriendArea(s);

                                float totalHeight = calculateTotalContentHeight();
                                float availableHeight = windowHeight - titleHeight - 2 * bigPadding;
                                float maxScrollable = Math.max(0, totalHeight - availableHeight);

                                float factor = 0;
                                if (maxScrollable > 0) {
                                    factor = MathHelper.clamp(fa.y / maxScrollable, 0, 1);
                                }

                                changeFriendsY(factor);
                                fa.light();
                            }
                        }, "+ add"
                    )
                }
            ).setCloseTask(this::resetCM));
            return true;
        }
        return false;
    }

    public static class FriendArea extends RenderArea {
        public String name;
        private float disableFactor = 0;
        private boolean delete = false;
        private boolean light = false;
        private float lightFactor = 0;

        private Timer timer = new Timer();

        public FriendArea(RenderArea parent, String name) {
            super(parent);
            this.name = name;
            this.showFactor = 0;

            areas.add(
                new ButtonArea(
                    this,
                    () -> {
                        if (Managers.FRIENDS.isDisable(name)) {
                            Managers.FRIENDS.enableFriend(name);
                        } else {
                            Managers.FRIENDS.disableFriend(name);
                        }
                    },
                    () -> Managers.FRIENDS.isDisable(name) ? "0" : "1",
                    () -> Managers.FRIENDS.isDisable(name) ? ColorUtils.fromRGB(25, 0, 0) : ColorUtils.fromRGB(0, 25, 0),
                    () -> hoveredFactor * showFactor,
                    true, true
                )
            );
        }

        @Override
        public void render(
            DrawContext context,
            int startX, int startY,
            int width, int height,
            double mouseX, double mouseY
        ) {
            int ld = (int) (100 * lightFactor);
            Render2D.fill(
                context,
                startX, startY,
                startX + width,
                startY + height,
                ColorUtils.fromRGB(ld, ld, ld, (50 + (30 * hoveredFactor)) * showFactor * parentArea.showFactor),
                bigPadding, 2
            );

            context.enableScissor(
                startX, startY,
                startX + width,
                startY + height
            );

            String d = "(disabled)";
            int textX = startX;
            int textWidth = (int) ((mc.textRenderer.getWidth(name) + (bigPadding * 2)) + (mc.textRenderer.getWidth(d) * disableFactor)) + bigPadding;
            if (textWidth + areas.getFirst().width + bigPadding > width) {
                textX -= (int) (((textWidth + areas.getFirst().width + bigPadding) - width) * hoveredFactor);
            }
            if (hoveredFactor != 0) {
                areas.getFirst().render(
                    context, (int) ((startX + width) - ((height - padding) * hoveredFactor)),
                    startY + padding, height - (padding * 2), height - (padding * 2),
                    mouseX, mouseY
                );
            }
            context.drawText(
                mc.textRenderer, name, textX + bigPadding,
                startY + ((height / 2) - (mc.textRenderer.fontHeight / 2)),
                ColorUtils.fromRGB(255, 255, 255, (200 - (100 * disableFactor)) * showFactor * parentArea.showFactor),
                false
            );
            if (disableFactor != 0) {
                context.drawText(
                    mc.textRenderer, d, (int) (textX + (mc.textRenderer.getWidth(name) + (bigPadding * 2)) * disableFactor),
                    startY + ((height / 2) - (mc.textRenderer.fontHeight / 2)),
                    ColorUtils.fromRGB(220, 200, 200, ((200 * disableFactor) * showFactor * parentArea.showFactor)),
                    false
                );
            }

            context.disableScissor();

            super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && checkHovered(mouseX, mouseY) && parentArea instanceof FriendsWindowArea fwa) {
                fwa.setCM(new ContextMenu(
                    this, this, new double[] {mouseX, mouseY},
                    new RenderArea[] {
                        new ButtonArea(
                            this,
                            () -> {
                                if (Managers.FRIENDS.isDisable(name)) {
                                    Managers.FRIENDS.enableFriend(name);
                                } else {
                                    Managers.FRIENDS.disableFriend(name);
                                }
                            },
                            () -> Managers.FRIENDS.isDisable(name) ? "enable" : "disable"
                        ),
                        new ButtonArea(
                            this,
                            () -> {
                                delete = true;
                                Managers.FRIENDS.removeFriend(name);
                            }, "🗑 delete",
                            ColorUtils.fromRGB(255, 230, 230)
                        ),
                        new ButtonInputArea(
                            this, (s) -> {
                                if (!FriendsManager.friends.contains(s)) {
                                    Managers.FRIENDS.addFriend(s);
                                    fwa.addFriendArea(new FriendArea(
                                        fwa, s
                                    ));
                                } else {
                                    FriendArea fa = fwa.getFriendArea(s);

                                    float totalHeight = fwa.calculateTotalContentHeight();
                                    float availableHeight = fwa.windowHeight - fwa.titleHeight - 2 * bigPadding;
                                    float maxScrollable = Math.max(0, totalHeight - availableHeight);

                                    float factor = 0;
                                    if (maxScrollable > 0) {
                                        factor = MathHelper.clamp(fa.y / maxScrollable, 0, 1);
                                    }

                                    fwa.changeFriendsY(factor);
                                    fa.light();
                                }
                            }, "+ add new"
                        )
                    }
                ).setCloseTask(fwa::resetCM));
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void animHandler() {
            disableFactor = AnimHelper.handle(FriendsManager.disables.contains(name), disableFactor);
            showFactor = AnimHelper.handle(!delete, showFactor);

            if (delete && showFactor == 0 && parentArea instanceof FriendsWindowArea fwa) {
                fwa.deleteFriendArea(this);
            }
            if (timer.getTimeMs() > 1000 && light) {
                light = false;
            }

            lightFactor = AnimHelper.handle(light, lightFactor);
        }

        public void light() {
            timer.reset();
            light = true;
        }
    }
}
