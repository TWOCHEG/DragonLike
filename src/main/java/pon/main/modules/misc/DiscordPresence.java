package pon.main.modules.misc;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import pon.main.Main;
import pon.main.managers.Managers;
import pon.main.modules.Parent;
import pon.main.modules.settings.Setting;
import pon.main.utils.discord.DiscordEventHandlers;
import pon.main.utils.discord.DiscordRPC;
import pon.main.utils.discord.DiscordRichPresence;
import pon.main.utils.math.Timer;

public final class DiscordPresence extends Parent {
    public enum sMode {custom, stats, version}

    public static Setting<Boolean> showIP = new Setting<>("show server", true);
    public static Setting<sMode> stateMode = new Setting<>("state mode", sMode.stats);
    public static Setting<String> state = new Setting<>("state", "че за нн параша?")
        .visible((s) -> stateMode.getValue().equals(sMode.custom));
    public static Setting<Boolean> nickname = new Setting<>("nickname", true);

    public static DiscordRichPresence presence = new DiscordRichPresence();
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;

    public static boolean started;
    private final Timer timer_delay = new Timer();
    String i;
    String[] rpc_options_en = {"parkour", "reporting cheaters", "touching grass", "asks how to bind", "reporting bugs", "watching Kilab"};
    String[] rpc_options_ru = {"паркурит", "репортит читеров", "трогает траву", "спрашивает как забиндить", "репортит баги", "смотрит Флюгера"};
    int randomInt;

    public DiscordPresence() {
        super("discord presence", Main.Categories.client);

        startRpc();
    }

    @Override
    public void onEnable() {
        startRpc();
    }

    @Override
    public void onDisable() {
        started = false;
        Thread.currentThread().interrupt();
        rpc.Discord_Shutdown();
    }

    public void startRpc() {
        if (!getEnable()) return;
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1093053626198523935", handlers, true, "пон");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "v" + Main.VERSION;
            rpc.Discord_UpdatePresence(presence);

            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && started) {
                    rpc.Discord_RunCallbacks();
                    presence.details = getDetails();

                    switch (stateMode.getValue()) {
                        case stats -> presence.state = "hacks: " + Managers.MODULE_MANAGER.enableModules().size() + " / " + Managers.MODULE_MANAGER.getModules().size();
                        case custom -> presence.state = state.getValue();
                        case version -> presence.state = "v" + Main.VERSION + " for mc 1.21.8";
                    }

                    if (nickname.getValue()) {
                        presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        presence.smallImageText = "";
                        presence.smallImageKey = "";
                    }

                    presence.button_label_1 = "download";
                    presence.button_url_1 = "https://github.com/TWOCHEG/DragonLike";
                    presence.largeImageKey = "https://raw.githubusercontent.com/TWOCHEG/DragonLike/refs/heads/main/src/main/resources/assets/main/icon.png";

                    rpc.Discord_UpdatePresence(presence);

                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }, "RPC-Handler").start();
        }
    }

    private String getDetails() {
        String result = "";

        if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen || mc.currentScreen instanceof TitleScreen) {
            if(timer_delay.passedMs(60 * 1000)){
                randomInt = (int)(Math.random() * (5 + 1) + 0);
                i = isRu() ? rpc_options_ru[randomInt] : rpc_options_en[randomInt];
                timer_delay.reset();
            }
            result = i;
        } else if (mc.getCurrentServerEntry() != null) {
            result = isRu() ? (showIP.getValue() ? "играет на " + mc.getCurrentServerEntry().address : "играет на сервере") : (showIP.getValue() ? "playing on " + mc.getCurrentServerEntry().address : "playing on server");
        } else if (mc.isInSingleplayer()) {
            result = isRu() ? "читерит в одиночке" : "singleplayer hacker";
        }
        return result;
    }
}
