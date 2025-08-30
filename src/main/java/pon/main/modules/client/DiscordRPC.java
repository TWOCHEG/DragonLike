package pon.main.modules.client;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import pon.main.Main;
import pon.main.modules.Parent;
import pon.main.modules.settings.SetsList;
import pon.main.modules.settings.Setting;
import pon.main.utils.discord.DiscordEventHandlers;
import pon.main.utils.discord.DiscordRichPresence;
import pon.main.utils.discord.IDiscordRPC;
import pon.main.utils.math.Timer;

import java.io.*;
import java.util.List;


public class DiscordRPC extends Parent {
    private static final IDiscordRPC rpc = IDiscordRPC.INSTANCE;
    public static Setting<Boolean> showIP = new Setting<>("show sv IP", true);
    public static SetsList<String> smode = new SetsList<>("state mode", List.of("stats", "version"));
    public static Setting<Boolean> nickname = new Setting<>("nickname", true);
    public static DiscordRichPresence presence = new DiscordRichPresence();
    public static boolean started;
    static String String1 = "none";
    private final Timer timer_delay = new Timer();
    private static Thread thread;
    String slov;
    String[] rpc_perebor_en = {"Parkour", "Reporting cheaters", "Touching grass", "Asks how to bind", "Reporting bugs", "Watching Kilab"};
    String[] rpc_perebor_ru = {"Паркурит", "Репортит читеров", "Трогает траву", "Спрашивает как забиндить", "Репортит баги", "Смотрит Флюгера"};
    int randomInt;

    public DiscordRPC() {
        super("discord RPC", Main.Categories.client);
    }

    @Override
    public void onDisable() {
        started = false;
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }

    @Override
    public void onEnable() {
        startRpc();
    }

    public void startRpc() {
        if (!getEnable()) return;
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1093053626198523935", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "v" + Main.VERSION;
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();
                    presence.details = getDetails();

                    if (smode.getValue().equals("stats")) {
                        presence.state = "Hacks: " + Main.MODULE_MANAGER.enableModules().size() + " / " + Main.MODULE_MANAGER.modules.size();
                    } else if (smode.getValue().equals("version")) {
                        presence.state = "v" + Main.VERSION;
                    }

                    if (nickname.getValue()) {
                        presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        presence.smallImageText = "";
                        presence.smallImageKey = "";
                    }

                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "https://github.com/TWOCHEG/DragonLike";
                    presence.largeImageKey = "https://raw.githubusercontent.com/TWOCHEG/DragonLike/refs/heads/main/src/main/resources/assets/main/icon.png";
                    rpc.Discord_UpdatePresence(presence);

                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }, "RPC-Handler");

            thread.start();
        }
    }

    private String getDetails() {
        String result = "";

        if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen || mc.currentScreen instanceof TitleScreen) {
            if(timer_delay.passedMs(60 * 1000)){
                randomInt = (int)(Math.random() * (5 - 0 + 1) + 0);
                slov = isRu() ? rpc_perebor_ru[randomInt] : rpc_perebor_en[randomInt];
                timer_delay.reset();
            }
            result = slov;
        } else if (mc.getCurrentServerEntry() != null) {
            result = isRu() ? (showIP.getValue() ? "Играет на " + mc.getCurrentServerEntry().address : "Играет на сервере") : (showIP.getValue() ? "Playing on " + mc.getCurrentServerEntry().address : "Playing on server");
        } else if (mc.isInSingleplayer()) {
            result = isRu() ? "Читерит в одиночке" : "SinglePlayer hacker";
        }
        return result;
    }
}
