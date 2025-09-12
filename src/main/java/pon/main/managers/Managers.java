package pon.main.managers;

import pon.main.modules.hud.HudModule;
import pon.main.modules.hud.components.*;

import pon.main.managers.client.*;
import pon.main.managers.main.*;

import pon.main.modules.client.*;
import pon.main.modules.misc.*;
import pon.main.modules.render.*;
import pon.main.modules.world.*;

import static pon.main.Main.EVENT_BUS;

public class Managers {
    public static final ConfigManager CONFIG = new ConfigManager();

    public static final ModuleManager MODULE_MANAGER = new ModuleManager(
        new Gui(),
        new Keybinds(),
        new FakePlayer(),
        new Nuker(),
        new Notify(),
        new AutoResponser(),
        new LagNotifier(),
        new DiscordPresence(),
        new Rotations(),
        new FreeCam(),
        new HudModule()
    );

    public static final HudManager HUD_MANAGER = new HudManager(
        new ArmorHud(),
        new ArrayListHud(),
        new CordsHud(),
        new FPSHud(),
        new TPSHud()
    );

    public static final ServerManager SERVER = new ServerManager();
    public static final FriendsManager FRIENDS = new FriendsManager();
    public static final PlayerManager PLAYER = new PlayerManager();

    public static final AsyncManager ASYNC = new AsyncManager();

    public static void init() {
        FRIENDS.loadFriends();
    }

    public static void subscribe() {
        EVENT_BUS.subscribe(SERVER);
        EVENT_BUS.subscribe(PLAYER);
        EVENT_BUS.subscribe(ASYNC);
    }
}
