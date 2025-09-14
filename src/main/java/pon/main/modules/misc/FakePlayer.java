package pon.main.modules.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import pon.main.Main;
import pon.main.events.impl.EventAttack;
import pon.main.events.impl.EventSync;
import pon.main.events.impl.PacketEvent;
import pon.main.modules.Parent;

import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.network.OtherClientPlayerEntity;
import pon.main.modules.settings.Setting;
import pon.main.utils.player.InventoryUtility;
import pon.main.utils.world.ExplosionUtility;

import java.util.*;

public class FakePlayer extends Parent {
    private final Setting<Boolean> copyInventory = new Setting<>("copy inventory", false);
    private Setting<Boolean> record = new Setting<>("record", false);
    private Setting<Boolean> play = new Setting<>("play", false);
    private Setting<Boolean> autoTotem = new Setting<>("auto totem", false);
    private Setting<String> name = new Setting<>("name", "your_friend");

    public static OtherClientPlayerEntity fakePlayer;

    public FakePlayer() {
        super("fake player", Main.Categories.misc);
    }

    private final List<PlayerState> positions = new ArrayList<>();

    int movementTick, deathTime;

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;

        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), name.getValue()));
        fakePlayer.copyPositionAndRotation(mc.player);

        if (copyInventory.getValue()) {
            fakePlayer.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack().copy());
            fakePlayer.setStackInHand(Hand.OFF_HAND, mc.player.getOffHandStack().copy());

            fakePlayer.getInventory().setStack(36, mc.player.getInventory().getStack(36).copy());
            fakePlayer.getInventory().setStack(37, mc.player.getInventory().getStack(37).copy());
            fakePlayer.getInventory().setStack(38, mc.player.getInventory().getStack(38).copy());
            fakePlayer.getInventory().setStack(39, mc.player.getInventory().getStack(39).copy());

            if (autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }

        mc.world.addEntity(fakePlayer);
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof ExplosionS2CPacket explosion && fakePlayer != null && fakePlayer.hurtTime == 0) {
            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - ExplosionUtility.getAutoCrystalDamage(new Vec3d(explosion.center().getX(), explosion.center().getY(), explosion.center().getZ()), fakePlayer, 0, false));
            if (fakePlayer.isDead()) {
                tryUseTotem();
            }
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (record.getValue()) {
            positions.add(new PlayerState(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch()));
            return;
        }
        if (fakePlayer != null) {
            if (play.getValue() && !positions.isEmpty()) {
                movementTick++;

                if (movementTick >= positions.size()) {
                    movementTick = 0;
                    return;
                }
                PlayerState p = positions.get(movementTick);
                fakePlayer.setYaw(p.yaw);
                fakePlayer.setPitch(p.pitch);
                fakePlayer.setHeadYaw(p.yaw);

                fakePlayer.updateTrackedPosition(p.x, p.y, p.z);
                fakePlayer.updateTrackedPositionAndAngles(new Vec3d(p.x, p.y, p.z), p.yaw, p.pitch);
            } else movementTick = 0;

            if (autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));

            if (fakePlayer.isDead()) {
                deathTime++;
                if (deathTime > 10) setEnable(false);
            }
        }
    }

    @EventHandler
    public void onAttack(EventAttack e) {
        if (fakePlayer != null && e.getEntity() == fakePlayer && fakePlayer.hurtTime == 0 && !e.isPre()) {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1f, 1f);

//            if (mc.player.fallDistance > 0 || Managers.MODULE_MANAGER.getModule(Criticals.class).getEnable())
//                mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1f, 1f);
            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
//            if (Managers.MODULE_MANAGER.getModule(KillAura.class).getAttackCooldown() >= 0.85)
//                fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - InventoryUtility.getHitDamage(mc.player.getMainHandStack(), fakePlayer));
//            else fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - 1f);
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - InventoryUtility.getHitDamage(mc.player.getMainHandStack(), fakePlayer));
            if (fakePlayer.isDead()) {
                tryUseTotem();
            }
        }
    }

    public void tryUseTotem() {
        ItemStack offhandStack = fakePlayer.getOffHandStack();
        boolean hasTotem = offhandStack.isOf(Items.TOTEM_OF_UNDYING);

        if (hasTotem) {
            fakePlayer.setHealth(10f);
            fakePlayer.clearStatusEffects();
            for (StatusEffectInstance effect : fakePlayer.getStatusEffects()) {
                if (!effect.getEffectType().value().isBeneficial()) {
                    fakePlayer.removeStatusEffect(effect.getEffectType());
                }
            }

            new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.player.networkHandler);
//            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
//                    SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
//
//            ClientPlayNetworkHandler networkHandler = mc.player.networkHandler;
//            if (networkHandler != null) {
//                networkHandler.sendPacket(new EntityStatusS2CPacket(fakePlayer, (byte) 35));
//            }
//
//            fakePlayer.getWorld().addParticleClient(
//                    ParticleTypes.TOTEM_OF_UNDYING,
//                    fakePlayer.getX(), fakePlayer.getY() + 1.0, fakePlayer.getZ(),
//                    0.0, 0.0, 0.0
//            );
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer == null) return;
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
        positions.clear();
        deathTime = 0;
    }

    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}
