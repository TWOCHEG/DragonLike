package pon.main.managers;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FriendsManager implements IManager {
    public static List<String> friends = new ArrayList<>();
    private static final Path FRIENDS_FILE_PATH = Path.of(System.getProperty("user.home"), ".dl", "friends.txt");

    public boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.equalsIgnoreCase(name));
    }

    public boolean isFriend(@NotNull PlayerEntity player) {
        return isFriend(player.getName().getString());
    }

    public void removeFriend(String name) {
        friends.remove(name);
        saveFriends();
    }

    public void addFriend(String friend) {
        friends.add(friend);
        saveFriends();
    }

    public List<String> getFriends() {
        return friends;
    }

    public void clear() {
        friends.clear();
        saveFriends();
    }

    public List<AbstractClientPlayerEntity> getNearFriends() {
        if (mc.world == null) return new ArrayList<>();

        return mc.world.getPlayers().stream()
                .filter(player -> friends.contains(player.getName().getString()))
                .toList();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveFriends() {
        try {
            Files.createDirectories(FRIENDS_FILE_PATH.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(
                FRIENDS_FILE_PATH,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )) {
                for (String friend : friends)
                    writer.write(friend + "\n");
            }
        } catch (Exception ignored) {}
    }

    public void loadFriends() {
        try {
            if (Files.exists(FRIENDS_FILE_PATH)) {
                try (BufferedReader reader = Files.newBufferedReader(FRIENDS_FILE_PATH)) {
                    friends.add(reader.readLine());
                }
            }
        } catch (Exception ignored) {}
    }
}
