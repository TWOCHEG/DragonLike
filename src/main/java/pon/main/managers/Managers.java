package pon.main.managers;

import static pon.main.Main.EVENT_BUS;

public class Managers {
    public static final ServerManager SERVER = new ServerManager();
    public static final FriendsManager FRIENDS = new FriendsManager();
    public static final PlayerManager PLAYER = new PlayerManager();

    public static final ConfigManager CONFIG = new ConfigManager();

    public static void init() {
        FRIENDS.loadFriends();
    }

    public static void subscribe() {
        EVENT_BUS.subscribe(SERVER);
        EVENT_BUS.subscribe(PLAYER);
    }
}
