package purr.purr.modules.combat;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.*;
import net.minecraft.entity.Entity;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

public class KillAura extends Parent {
    public final Setting<Float> attackRange = new Setting<>("range", 3f, 1f, 6f);
    public final Setting<Float> wallRange = new Setting<>("walls range", 0f, 0f, 6f);
    public final Setting<Boolean> elytra = new Setting<>("elytra override",true);
    public final Setting<Float> elytraAttackRange = new Setting<>("elytra range", 1.5f, 0f, 6f).visibleIf(m -> elytra.getValue());
    public final Setting<Float> elytraWallRange = new Setting<>("elytra walls range", 0f, 0f, 6f).visibleIf(m -> elytra.getValue());
    public final ListSetting<String> wallsBypass = new ListSetting<>(
        "walls bypass",
        new ArrayList<>(List.of("off", "V1", "V2"))
    );
    public final Setting<Integer> fov = new Setting<>("FOV", 180, 1, 360);
    public final ListSetting<String> rotationMode = new ListSetting<>(
        "rotation mode",
        new ArrayList<>(List.of("track", "interact", "grim", "none"))
    );
    public final Setting<Integer> interactTicks = new Setting<>("interact ticks", 3, 1, 10).visibleIf(m -> rotationMode.getValue().equals("interact"));
    public final ListSetting<String> switchMode = new ListSetting<>(
        "auto weapon",
        new ArrayList<>(List.of("normal", "none", "silent"))
    );
    public final Setting<Boolean> onlyWeapon = new Setting<>("only weapon", false).visibleIf(m -> !switchMode.getValue().equals("silent"));
    public final Group smartCrit = new Group("smart crit");
    public final Setting<Boolean> onlySpace = new Setting<>("only crit", false).addToGroup(smartCrit);
    public final Setting<Boolean> autoJump = new Setting<>("auto jump", false).addToGroup(smartCrit);
    public final Setting<Boolean> shieldBreaker = new Setting<>("shield breaker", true);
    public final Setting<Boolean> pauseWhileEating = new Setting<>("pause while eating", false);
    public final Setting<Boolean> tpsSync = new Setting<>("TPS sync", false);
    public final Setting<Boolean> clientLook = new Setting<>("client look", false);
    public final Setting<Boolean> pauseBaritone = new Setting<>("pause baritone", false);
    public final Setting<Boolean> oldDelay = new Setting<>("old delay", false);
    public final Setting<Integer> minCPS = new Setting<>("min CPS", 7, 1, 20).visibleIf(m -> oldDelay.getValue());
    public final Setting<Integer> maxCPS = new Setting<>("max CPS", 12, 1, 20).visibleIf(m -> oldDelay.getValue());

    public final ListSetting<String> sort = new ListSetting<>(
        "sort",
        new ArrayList<>(List.of("lowest distance", "highest distance", "lowest health", "highest health", "lowest durability", "highest durability", "look"))
    );
    public final Setting<Boolean> lockTarget = new Setting<>("lock target", true);
    public final Setting<Boolean> elytraTarget = new Setting<>("elytra target", true);

    /*   ADVANCED   */
    public final Group advanced = new Group("advanced");
    public final Setting<Float> aimRange = new Setting<>("aim range", 3.1f, 0f, 6f).addToGroup(advanced);
    public final Setting<Boolean> randomHitDelay = new Setting<>("random hit delay", false).addToGroup(advanced);
    public final Setting<Boolean> pauseInInventory = new Setting<>("pause in inventory", false).addToGroup(advanced);
    public final Setting<Boolean> dropSprint = new Setting<>("drop sprint", false).addToGroup(advanced);
    public final Setting<Boolean> returnSprint = new Setting<>("return sprint", true).addToGroup(advanced).visibleIf(m -> dropSprint.getValue());
    public final ListSetting<String> rayTrace = (ListSetting<String>) new ListSetting<>(
        "ray trace",
        new ArrayList<>(List.of("off", "only target", "all entities"))
    ).addToGroup(advanced);
    public final Setting<Boolean> grimRayTrace = new Setting<>("grim ray trace", true).addToGroup(advanced);
    public final Setting<Boolean> unpressShield = new Setting<>("unpress shield", true).addToGroup(advanced);
    public final Setting<Boolean> deathDisable = new Setting<>("disable on death", true).addToGroup(advanced);
    public final Setting<Boolean> tpDisable = new Setting<>("TP disable", false).addToGroup(advanced);
    public final Setting<Boolean> pullDown = new Setting<>("fast fall", false).addToGroup(advanced);
    public final Setting<Boolean> onlyJumpBoost = new Setting<>("only jump boost", false).addToGroup(advanced).visibleIf(m -> pullDown.getValue());
    public final Setting<Float> pullValue = new Setting<>("pull value", 3f, 0f, 20f).addToGroup(advanced).visibleIf(m -> pullDown.getValue());
    public final ListSetting<String> attackHand = (ListSetting<String>) new ListSetting<>(
        "attack hand",
        new ArrayList<>(List.of("main hand", "off hand", "none"))
    ).addToGroup(advanced);
    public final ListSetting<String> accelerateOnHit = (ListSetting<String>) new ListSetting<>(
        "accelerate on hit",
        new ArrayList<>(List.of("off", "yaw", "pitch", "both"))
    ).addToGroup(advanced).addToGroup(advanced);
    public final Header header = (Header) new Header("aim assist").addToGroup(advanced);
    public final Setting<Integer> minYawStep = new Setting<>("min yaw step", 65, 1, 180).addToGroup(advanced);
    public final Setting<Integer> maxYawStep = new Setting<>("max yaw step", 75, 1, 180).addToGroup(advanced);
    public final Setting<Float> aimedPitchStep = new Setting<>("aimed pitch step", 1f, 0f, 90f).addToGroup(advanced);
    public final Setting<Float> maxPitchStep = new Setting<>("max pitch step", 8f, 1f, 90f).addToGroup(advanced);
    public final Setting<Float> pitchAccelerate = new Setting<>("pitch accelerate", 1.65f, 1f, 10f).addToGroup(advanced);
    public final Setting<Float> attackCooldown = new Setting<>("attack cooldown", 0.9f, 0.5f, 1f).addToGroup(advanced);
    public final Setting<Float> attackBaseTime = new Setting<>("attack base time", 0.5f, 0f, 2f).addToGroup(advanced);
    public final Setting<Integer> attackTickLimit = new Setting<>("attack tick limit", 11, 0, 20).addToGroup(advanced);
    public final Setting<Float> critFallDistance = new Setting<>("crit fall distance", 0f, 0f, 1f).addToGroup(advanced);

    /*   TARGETS   */
    public final Group targets = new Group("targets");
    public final Setting<Boolean> Players = new Setting<>("players", true).addToGroup(targets);
    public final Setting<Boolean> Mobs = new Setting<>("mobs", true).addToGroup(targets);
    public final Setting<Boolean> Animals = new Setting<>("animals", true).addToGroup(targets);
    public final Setting<Boolean> Villagers = new Setting<>("villagers", true).addToGroup(targets);
    public final Setting<Boolean> Slimes = new Setting<>("slimes", true).addToGroup(targets);
    public final Setting<Boolean> hostiles = new Setting<>("hostiles", true).addToGroup(targets);
    public final Setting<Boolean> onlyAngry = new Setting<>("only angry hostiles", true).addToGroup(targets).visibleIf(m -> hostiles.getValue());
    public final Setting<Boolean> Projectiles = new Setting<>("projectiles", true).addToGroup(targets);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("ignore invisible entities", false).addToGroup(targets);
    public final Setting<Boolean> ignoreNamed = new Setting<>("ignore named", false).addToGroup(targets);
    public final Setting<Boolean> ignoreTeam = new Setting<>("ignore team", false).addToGroup(targets);
    public final Setting<Boolean> ignoreCreative = new Setting<>("ignore creative", true).addToGroup(targets);
    public final Setting<Boolean> ignoreNaked = new Setting<>("ignore naked", false).addToGroup(targets);
    public final Setting<Boolean> ignoreShield = new Setting<>("attack shielding entities", true).addToGroup(targets);

    public static Entity target;

    public float rotationYaw;
    public float rotationPitch;
    public float pitchAcceleration = 1f;

    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;

    private int hitTicks;
    private int trackticks;
    private boolean lookingAtHitbox;

    private Timer delayTimer = new Timer();
    private Timer pauseTimer = new Timer();

    public Box resolvedBox;
    static boolean wasTargeted = false;

    public KillAura() {
        super("kill aura", "combat");
    }

    @Override
    public void onEnable() {
        target = null;
        lookingAtHitbox = false;
        rotationPoint = Vec3d.ZERO;
        rotationMotion = Vec3d.ZERO;
        rotationYaw = client.player.getYaw();
        rotationPitch = client.player.getPitch();
        delayTimer = null;
        delayTimer = new Timer();
    }

    @Override
    public void onDisable() {
        target = null;
    }

//    public Entity findTarget() {
//        List<Entity> validTargets = new ArrayList<>();
//        Vec3d playerPos = client.player.getPos();
//        float playerYaw = client.player.getYaw();
//
//        for (Entity entity : client.world.getEntities()) {
//            if (!isTargetTypeEnabled(entity)) continue;
//
//            if (shouldIgnoreEntity(entity)) continue;
//
//            float distance = (float) playerPos.distanceTo(entity.getPos());
//            float effectiveRange = elytra.getValue() && entity.equals(target) ?
//                    elytraAttackRange.getValue() : attackRange.getValue();
//
//            if (distance > effectiveRange) continue;
//
//            if (!isInFOV(entity, playerYaw, playerPos)) continue;
//
//            validTargets.add(entity);
//        }
//
//        sortTargets(validTargets, playerPos);
//
//        return validTargets.isEmpty() ? null : validTargets.get(0);
//    }
//    private boolean isSameTeam(Entity entity) {
//        return false;
//    }
//    private boolean isTargetTypeEnabled(Entity entity) {
//        if (entity instanceof PlayerEntity && !Players.getValue()) return false;
//        if (entity instanceof MobEntity && !Mobs.getValue()) return false;
//        if (entity instanceof AnimalEntity && !Animals.getValue()) return false;
//        if (entity instanceof VillagerEntity && !Villagers.getValue()) return false;
//        if (entity instanceof SlimeEntity && !Slimes.getValue()) return false;
//        if (entity instanceof HostileEntity && hostiles.getValue() &&
//                onlyAngry.getValue() && !((HostileEntity)entity).isAngryAt(client.getServer().getWorld(client.world.getRegistryKey()), client.player)) return false;
//        return true;
//    }
//    private boolean shouldIgnoreEntity(Entity entity) {
//        if (ignoreInvisible.getValue() && !entity.isInvisible()) return true;
//        if (ignoreNamed.getValue() && entity.hasCustomName()) return true;
//        if (ignoreTeam.getValue() && isSameTeam(entity)) return true;
//        if (ignoreCreative.getValue() && entity instanceof PlayerEntity &&
//                ((PlayerEntity)entity).isCreative()) return true;
//        if (ignoreNaked.getValue() && hasArmor(entity)) return true;
//        if (ignoreShield.getValue() && isShieldBlocking(entity)) return true;
//        return false;
//    }
//    private boolean isInFOV(Entity entity, float playerYaw, Vec3d playerPos) {
//        Vec3d entityDir = entity.getPos().subtract(playerPos).normalize();
//        double angle = Math.toDegrees(Math.acos(entityDir.dotProduct(
//                Vec3d.fromPolar(playerYaw, 0))));
//        return angle <= fov.getValue() / 2f;
//    }
//    private void sortTargets(List<Entity> targets, Vec3d playerPos) {
//        String sortMode = sort.getValue();
//        targets.sort((a, b) -> {
//            if (!a.isLiving() || !b.isLiving()) {
//                return 0;
//            }
//
//            float distA = (float) playerPos.distanceTo(a.getPos());
//            float distB = (float) playerPos.distanceTo(b.getPos());
//
//            return switch (sortMode) {
//                case "lowest distance" -> Float.compare(distA, distB);
//                case "highest distance" -> Float.compare(distB, distA);
//                case "lowest health" -> Float.compare(((LivingEntity) a).getHealth(), ((LivingEntity) b).getHealth());
//                case "highest health" -> Float.compare(((LivingEntity) b).getHealth(), ((LivingEntity) a).getHealth());
//                case "lowest durability" -> Integer.compare(
//                        getArmorDurability(a), getArmorDurability(b));
//                case "highest durability" -> Integer.compare(
//                        getArmorDurability(b), getArmorDurability(a));
//                case "look" -> Float.compare(
//                        getAngleToEntity(a, playerPos), getAngleToEntity(b, playerPos));
//                default -> 0;
//            };
//        });
//    }
//    private boolean hasArmor(Entity entity) {
//        if (!(entity instanceof LivingEntity living)) return false;
//
//        for (EquipmentSlot slot : EquipmentSlot.values()) {
//            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
//                ItemStack stack = living.getEquippedStack(slot);
//                if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    private boolean isShieldBlocking(Entity entity) {
//        if (!(entity instanceof LivingEntity living)) return false;
//
//        ItemStack offhand = living.getOffHandStack();
//        return !offhand.isEmpty() && offhand.getItem() == Items.SHIELD &&
//                living.isUsingItem() && living.getActiveItem() == offhand;
//    }
//    private int getArmorDurability(Entity entity) {
//        if (!(entity instanceof LivingEntity living)) return 0;
//
//        int durability = 0;
//
//        for (EquipmentSlot slot : EquipmentSlot.values()) {
//            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
//                ItemStack stack = living.getEquippedStack(slot);
//                if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
//                    durability += stack.getDamage();
//                }
//            }
//        }
//        return durability;
//    }
//    private float getAngleToEntity(Entity entity, Vec3d playerPos) {
//        double dx = entity.getX() - playerPos.x;
//        double dz = entity.getZ() - playerPos.z;
//
//        double targetAngle = Math.atan2(dz, dx) * 180 / Math.PI;
//        float angleDiff = MathHelper.wrapDegrees(client.player.getYaw() - (float)targetAngle);
//
//        return Math.abs(angleDiff);
//    }
}

