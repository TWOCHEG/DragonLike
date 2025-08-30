package pon.main.managers;

import pon.main.Main;

public class Managers {
    public static final RotationManager ROTATIONS = new RotationManager();
    public static final ServerManager SERVER_MANAGER = new ServerManager();
    public static final FriendsManager FRIENDS = new FriendsManager();
    public static final ConfigManager CONFIG = new ConfigManager();

    public static void init() {
        FRIENDS.loadFriends();
        Main.EVENT_BUS.subscribe(ROTATIONS);
    }
}
