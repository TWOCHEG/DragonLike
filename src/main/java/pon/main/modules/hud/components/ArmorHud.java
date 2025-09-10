package pon.main.modules.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import pon.main.gui.components.CategoryArea;
import pon.main.gui.components.RenderArea;
import pon.main.modules.Parent;
import pon.main.modules.hud.Hud;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.render.Render2D;

public class ArmorHud extends HudArea{
    public Setting<Boolean> linkedToHotbar = new Setting<>("stick to hotbar", true);

    public ArmorHud(Hud hud) {
        super(hud);
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        height = 22;
        int areaWidth = height - (padding * 2);
        areas.clear();
        ItemStack[] armor = new ItemStack[]{getItem(EquipmentSlot.FEET), getItem(EquipmentSlot.LEGS), getItem(EquipmentSlot.CHEST), getItem(EquipmentSlot.HEAD)};

        for (ItemStack item : armor) {
            areas.add(new ArmorElement(this, item));
        }

        for (RenderArea area : areas) {
            width += padding + areaWidth;
        }
        width += padding;

        if (linkedToHotbar.getValue() && !Parent.fullNullCheck()) {
            x = (mc.getWindow().getScaledWidth() / 2) + 10;
            y = mc.getWindow().getScaledHeight() - 62 - (isOxygenHudVisible(mc.player) ? 10 : 0);
        }

        Render2D.fill(
            context, x, y,
            x + width, y + height,
            CategoryArea.makeAColor((100 + (30 * draggedFactor)) * showFactor),
            bigPadding, 2
        );

        int currentX = x + padding;
        for (RenderArea area : areas) {
            area.render(context, currentX, y + padding, areaWidth, areaWidth, mouseX, mouseY);
            currentX += areaWidth + padding;
        }

        super.render(context, x, y, width, height, mouseX, mouseY);
    }

    public static boolean isOxygenHudVisible(PlayerEntity player) {
        // аааа сука почему эта хуйня не работает
        if (!player.isSubmergedIn(FluidTags.WATER)) return false;
        if (player.getAir() >= player.getMaxAir()) return false;
        if (player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) return false;
        return true;
    }

    private ItemStack getItem(EquipmentSlot slot) {
        if (Parent.fullNullCheck()) return null;
        ItemStack stack = mc.player.getEquippedStack(slot);
        return stack;
    }

    public class ArmorElement extends RenderArea {
        private ItemStack item;
        public ArmorElement(RenderArea parentArea, ItemStack item) {
            super(parentArea);
            this.item = item;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
            if (item != null && item != ItemStack.EMPTY) {
                context.drawItem(
                    item, x + ((width / 2) - 7), y - 1
                );
                Render2D.fill(
                    context, x, (y + height) - 3,
                    x + width, y + height,
                    ColorUtils.fromRGB(100, 100, 100, 100 * showFactor),
                    1, 2
                );
                float durability = getDurabilityPercentage(item);
                Render2D.fill(
                    context, x, (y + height) - 3,
                    (int) (x + (width * durability)), y + height,
                    ColorUtils.fromRGB(
                        (int) (50 + (100 * (1 - durability))),
                        (int) (50 + (100 * durability)),
                        50,
                        150 * showFactor
                    ),
                    1, 2
                );
            } else {
                String s = "-";
                context.drawText(
                    mc.textRenderer, s,
                    x + ((width / 2) - (mc.textRenderer.getWidth(s) / 2)),
                    y + ((height / 2) - (mc.textRenderer.fontHeight / 2)),
                    ColorUtils.fromRGB(255, 255, 255, 100 * showFactor),
                    false
                );
            }

            super.render(context, x, y, width, height, mouseX, mouseY);
        }

        public static float getDurabilityPercentage(ItemStack stack) {
            if (!stack.isDamageable()) {
                return 1f;
            }

            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) {
                return 1f;
            }

            int damage = stack.getDamage();
            int durabilityLeft = maxDamage - damage;

            durabilityLeft = Math.max(0, Math.min(durabilityLeft, maxDamage));

            return (float) durabilityLeft / maxDamage;
        }
    }
}
