package pon.main.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.*;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.*;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.core.script.Script;
import pon.main.Main;
import pon.main.events.impl.EventTick;
import pon.main.gui.ModulesGui;
import pon.main.modules.Parent;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import pon.main.modules.settings.Setting;
import pon.main.utils.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class DiscordPresence extends Parent {
    public enum SelectMode {
        random,
        sequential
    }

    private final Setting<Integer> line1UpdateDelay = new Setting<>("line 1 update delay", 200, 10, 200);

    private final Setting<SelectMode> line1SelectMode = new Setting<>(SelectMode.random);

    private final Setting<String> line2Strings = new Setting<>("line 2 messages", List.of("пон", "норм", "8============"));

    private final Setting<Integer> line2UpdateDelay = new Setting<>("line 2 update delay", 60, 10, 200);

    private final Setting<SelectMode> line2SelectMode = new Setting<>("line 2 select mode", SelectMode.sequential);

    private static final RichPresence rpc = new RichPresence();
    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    public static final List<Pair<String, String>> customStates = new ArrayList<>();

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public DiscordPresence() {
        super("discord presence", Main.Categories.misc);
        currentSmallImage = SmallImage.Snail;
    }

    /** Registers a custom state to be used when the current screen is a class in the specified package. */
    public static void registerCustomState(String packageName, String state) {
        for (var pair : customStates) {
            if (pair.getLeft().equals(packageName)) {
                pair.setRight(state);
                return;
            }
        }

        customStates.add(new Pair<>(packageName, state));
    }

    /** The package name must match exactly to the one provided through {@link #registerCustomState(String, String)}. */
    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> pair.getLeft().equals(packageName));
    }

    @Override
    public void onEnable() {
        DiscordIPC.start(835240968533049424L, null);

        rpc.setStart(System.currentTimeMillis() / 1000L);

        String largeText = "%s %s".formatted("DragonLike", Main.VERSION);
        largeText += " Build: " + Main.VERSION;
        rpc.setLargeImage("meteor_client", largeText);

        currentSmallImage = SmallImage.Snail;

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDisable() {
        DiscordIPC.stop();
    }

    @EventHandler
    private void onTick(EventTick e) {
        boolean update = false;

        // Image
        if (ticks >= 200 || forceUpdate) {
            currentSmallImage = currentSmallImage.next();
            currentSmallImage.apply();
            update = true;

            ticks = 0;
        }
        else ticks++;

        if (line1Ticks >= line1UpdateDelay.getValue() || forceUpdate) {
            if (!line1Scripts.isEmpty()) {
                int i = MathUtils.random(0, line1Scripts.size());
                if (line1SelectMode.getValue() == SelectMode.sequential) {
                    if (line1I >= line1Scripts.size()) line1I = 0;
                    i = line1I++;
                }

                String message = "пон1";
                if (message != null) rpc.setDetails(message);
            }
            update = true;

            line1Ticks = 0;
        } else line1Ticks++;

        // Line 2
        if (line2Ticks >= line2UpdateDelay.getValue() || forceUpdate) {
            if (!line2Scripts.isEmpty()) {
                int i = MathUtils.random(0, line2Scripts.size());
                if (line2SelectMode.getValue() == SelectMode.sequential) {
                    if (line2I >= line2Scripts.size()) line2I = 0;
                    i = line2I++;
                }

                String message = "пон";
                if (message != null) rpc.setState(message);
            }
            update = true;

            line2Ticks = 0;
        } else line2Ticks++;

        if (!lastWasInMainMenu) {
            rpc.setDetails("DragonLike" + " " + Main.VERSION);

            if (mc.currentScreen instanceof TitleScreen) rpc.setState("Looking at title screen");
            else if (mc.currentScreen instanceof SelectWorldScreen) rpc.setState("Selecting world");
            else if (mc.currentScreen instanceof CreateWorldScreen || mc.currentScreen instanceof EditGameRulesScreen) rpc.setState("Creating world");
            else if (mc.currentScreen instanceof EditWorldScreen) rpc.setState("Editing world");
            else if (mc.currentScreen instanceof LevelLoadingScreen) rpc.setState("Loading world");
            else if (mc.currentScreen instanceof MultiplayerScreen) rpc.setState("Selecting server");
            else if (mc.currentScreen instanceof AddServerScreen) rpc.setState("Adding server");
            else if (mc.currentScreen instanceof ConnectScreen || mc.currentScreen instanceof DirectConnectScreen) rpc.setState("Connecting to server");
            else if (mc.currentScreen instanceof ModulesGui) rpc.setState("Browsing DragonLike's GUI");
            else if (mc.currentScreen instanceof OptionsScreen || mc.currentScreen instanceof SkinOptionsScreen || mc.currentScreen instanceof SoundOptionsScreen || mc.currentScreen instanceof VideoOptionsScreen || mc.currentScreen instanceof ControlsOptionsScreen || mc.currentScreen instanceof LanguageOptionsScreen || mc.currentScreen instanceof ChatOptionsScreen || mc.currentScreen instanceof PackScreen || mc.currentScreen instanceof AccessibilityOptionsScreen) rpc.setState("Changing options");
            else if (mc.currentScreen instanceof CreditsScreen) rpc.setState("Reading credits");
            else if (mc.currentScreen instanceof RealmsScreen) rpc.setState("Browsing Realms");
            else {
                boolean setState = false;
                if (mc.currentScreen != null) {
                    String className = mc.currentScreen.getClass().getName();
                    for (var pair : customStates) {
                        if (className.startsWith(pair.getLeft())) {
                            rpc.setState(pair.getRight());
                            setState = true;
                            break;
                        }
                    }
                }
                if (!setState) rpc.setState("In main menu");
            }

            update = true;
        }

        if (update) DiscordIPC.setActivity(rpc);
        forceUpdate = false;
    }

    private enum SmallImage {
        MineGame("minegame", "MineGame159"),
        Snail("seasnail", "seasnail8169");

        private final String key, text;

        SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            rpc.setSmallImage(key, text);
        }

        SmallImage next() {
            if (this == MineGame) return Snail;
            return MineGame;
        }
    }
}
