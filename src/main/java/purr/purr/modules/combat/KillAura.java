/*
https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/features/modules/combat/Aura.java
*/
package purr.purr.modules.combat;

import net.minecraft.util.math.Vec3d;
import purr.purr.modules.Parent;
import purr.purr.modules.settings.*;

import javax.swing.*;
import javax.swing.text.html.parser.Entity;
import java.util.*;
import java.util.Timer;

public class KillAura extends Parent {
    public final Setting<Float> attackRange = new Setting<>("range", 3.0f, 1f, 6.0f);
    public final Setting<Float> wallRange = new Setting<>("walls range", 0f, 0f, 6.0f);
    public final Setting<Boolean> elytra = new Setting<>("elytra override",true);
    public final Setting<Float> elytraAttackRange = new Setting<>("elytra walls range", 1.5f, 1f, 6.0f).visibleIf(m -> elytra.getValue());
    public final Setting<Float> elytraWallRange = new Setting<>("elytra walls range", 0f, 0f, 6.0f).visibleIf(m -> elytra.getValue());
    public final ListSetting<String> wallsBypass = new ListSetting<>(
        "walls bypass",
        new ArrayList<>(List.of("off", "V1", "V2"))
    );
    public final Setting<Integer> fov = new Setting<>("FOV", 180, 1, 180);
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

//    public final Setting<ESP> esp = new Setting<>("ESP", ESP.ThunderHack);
//    public final Setting<SettingGroup> espGroup = new Setting<>("ESPSettings", new SettingGroup(false, 0), v -> esp.is(ESP.ThunderHackV2));
//    public final Setting<Integer> espLength = new Setting<>("ESPLength", 14, 1, 40, v -> esp.is(ESP.ThunderHackV2)).addToGroup(espGroup);
//    public final Setting<Integer> espFactor = new Setting<>("ESPFactor", 8, 1, 20, v -> esp.is(ESP.ThunderHackV2)).addToGroup(espGroup);
//    public final Setting<Float> espShaking = new Setting<>("ESPShaking", 1.8f, 1.5f, 10f, v -> esp.is(ESP.ThunderHackV2)).addToGroup(espGroup);
//    public final Setting<Float> espAmplitude = new Setting<>("ESPAmplitude", 3f, 0.1f, 8f, v -> esp.is(ESP.ThunderHackV2)).addToGroup(espGroup);

    public final ListSetting<String> sort = new ListSetting<>(
        "sort",
        new ArrayList<>(List.of("lowest distance", "highest distance", "lowest health", "highest health", "lowest durability", "highest durability", "look"))
    );
    public final Setting<Boolean> lockTarget = new Setting<>("lock target", true);
    public final Setting<Boolean> elytraTarget = new Setting<>("elytra target", true);

    /*   ADVANCED   */
    public final Group advanced = new Group("advanced");
    public final Header test = (Header) new Header("эта хуйня тест адаптивного расширения окна").addToGroup(advanced);
    public final Header test2 = (Header) new Header("если я забыл ее убрать мне похуй").addToGroup(advanced);
    public final Setting<Float> aimRange = new Setting<>("aim range", 3.1f, 0f, 6.0f).addToGroup(advanced);
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

    private final Timer delayTimer = new Timer();
    private final Timer pauseTimer = new Timer();

    public Box resolvedBox;
    static boolean wasTargeted = false;

    public KillAura() {
        super("kill aura", "combat");
    }
}

