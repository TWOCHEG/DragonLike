package pon.purr.modules.render;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import pon.purr.Purr;
import pon.purr.events.impl.EventSync;
import pon.purr.events.impl.PacketEvent;
import pon.purr.managers.Managers;
import pon.purr.modules.Parent;
import pon.purr.modules.settings.Setting;
import pon.purr.modules.ui.Notify;

import java.util.ArrayList;
import java.util.List;

public class NoRender extends Parent {
    public NoRender() {
        super("NoRender", Purr.Categories.render);
    }

    public final Setting<Boolean> disableGuiBackGround = new Setting<>("noGuiBackGround", true);
    public final Setting<Boolean> noSwing = new Setting<>("NoHandSwing", false);
    public final Setting<Boolean> auto = new Setting<>("Auto", false);
    public final Setting<Boolean> hurtCam = new Setting<>("HurtCam", true);
    public final Setting<Boolean> potions = new Setting<>("Potions", false);
    public final Setting<Boolean> xp = new Setting<>("XP", false);
    public final Setting<Boolean> arrows = new Setting<>("Arrows", false);
    public final Setting<Boolean> eggs = new Setting<>("Eggs", false);
    public final Setting<Boolean> elderGuardian = new Setting<>("Guardian", false);
    public final Setting<Boolean> vignette = new Setting<>("Vignette", true);
    public final Setting<Boolean> portal = new Setting<>("Portal", true);
    public final Setting<Boolean> explosions = new Setting<>("Explosions", false);
    public final Setting<Boolean> campFire = new Setting<>("CampFire", false);
    public final Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
    public final Setting<Boolean> armor = new Setting<>("Armor", false);
    public final Setting<Boolean> bossbar = new Setting<>("Bossbar", false);
    public final Setting<Boolean> fireOverlay = new Setting<>("FireOverlay", false);
    public final Setting<Boolean> waterOverlay = new Setting<>("WaterOverlay", false);
    public final Setting<Boolean> blockOverlay = new Setting<>("BlockOverlay", false);
    public final Setting<Boolean> nausea = new Setting<>("Nausea", false);
    public final Setting<Boolean> blindness = new Setting<>("Blindness", false);
    public final Setting<Boolean> fog = new Setting<>("Fog", false);
    public final Setting<Boolean> darkness = new Setting<>("Darkness", false);
    public final Setting<Boolean> items = new Setting<>("Items", false);
    public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    public final Setting<Boolean> fireEntity = new Setting<>("FireEntity", true);
    public final Setting<Boolean> breakParticles = new Setting<>("BreakParticles", true);
    public final Setting<Boolean> antiTitle = new Setting<>("AntiTitle", false);
    public final Setting<Boolean> antiPlayerCollision = new Setting<>("AntiPlayerCollision", true);
    public final Setting<Boolean> noScoreBoard = new Setting<>("NoScoreBoard", true);
    public final Setting<Boolean> signText = new Setting<>("SignText", false);
    public final Setting<Boolean> noWeather = new Setting<>("NoWeather", false);
    public final Setting<Boolean> noArmorStands = new Setting<>("NoArmorStands", false);
    public final Setting<Boolean> spawnerEntity = new Setting<>("SpawnerEntity", false);
    public final Setting<Boolean> hotbarItemName = new Setting<>("HotbarItemName", false);

    private int potionCouter, xpCounter, arrowCounter, itemsCounter;

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof TitleS2CPacket && antiTitle.getValue())
            e.cancel();
    }

    @EventHandler
    public void onSync(EventSync e) {
        for (Entity ent : Managers.ASYNC.getAsyncEntities()) {
            if (ent instanceof PotionEntity) {
                potionCouter++;
                if (potions.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ExperienceBottleEntity) {
                xpCounter++;
                if (xp.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof EndCrystalEntity) {
                if (crystals.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ArrowEntity) {
                arrowCounter++;
                if (arrows.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof EggEntity) {
                if (eggs.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ItemEntity) {
                itemsCounter++;
                if (items.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
            if (ent instanceof ArmorStandEntity){
                if(noArmorStands.getValue()) mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
            }
        }

        if (auto.getValue()) {
            if (itemsCounter > 16 || arrowCounter > 64 || xpCounter > 16 || potionCouter > 8)
                notify(new Notify.NotifyData("items limit reached! removing...", Notify.NotifyType.Important, getNotifyLiveTime()));

            List<Integer> toRemove = new ArrayList<>();

            for (Entity ent  : Managers.ASYNC.getAsyncEntities()) {
                if (ent instanceof ArrowEntity && arrowCounter > 64) toRemove.add(ent.getId());
                if (ent instanceof ItemEntity && itemsCounter > 16) toRemove.add(ent.getId());
                if (ent instanceof ExperienceBottleEntity && xpCounter > 16) toRemove.add(ent.getId());
                if (ent instanceof PotionEntity && potionCouter > 8) toRemove.add(ent.getId());
            }

            try {
                toRemove.forEach(id -> mc.world.removeEntity(id, Entity.RemovalReason.KILLED));
            } catch (Exception ignored) {
            }
        }

        arrowCounter = 0;
        itemsCounter = 0;
        potionCouter = 0;
        xpCounter = 0;
    }
}

