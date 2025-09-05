package pon.main.managers;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FriendsManager implements IManager {
    public static LinkedList<String> friends = new LinkedList<>();
    public static LinkedList<String> disables = new LinkedList<>();

    public static final Path DATA_FILE_PATH = Path.of(System.getProperty("user.home"), ".dl", "friends_data.json");

    public boolean isFriend(String name) {
        return isFriend(name) && !disables.contains(name);
    }
    public boolean isFriend(@NotNull PlayerEntity player) {
        return isFriend(player.getName().getString());
    }

    public void removeFriend(String name) {
        disables.remove(name);
        friends.remove(name);
        saveFriends();
    }

    public void disableFriend(String name) {
        disables.add(name);
        saveFriends();
    }
    public void enableFriend(String name) {
        disables.remove(name);
        saveFriends();
    }
    public boolean isDisable(String name) {
        return disables.contains(name);
    }

    public void addFriend(String friend) {
        if (!friends.contains(friend)) {
            friends.add(friend);
            saveFriends();
        }
    }

    public List<String> getFriends() {
        LinkedList<String> friendsCopy = (LinkedList<String>) friends.clone();
        friendsCopy.removeAll(disables);
        return friendsCopy;
    }

    public void clear() {
        disables.clear();
        friends.clear();
        saveFriends();
    }

    public List<AbstractClientPlayerEntity> getNearFriends() {
        if (mc.world == null) return new ArrayList<>();

        return mc.world.getPlayers().stream()
                .filter(player -> getFriends().contains(player.getName().getString()))
                .toList();
    }

    public void saveFriends() {
        Path parentDir = DATA_FILE_PATH.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (Exception ignore) {}
        }
        Map<String, Object> data = new HashMap<>();
        data.put("friends", friends);
        data.put("disables", disables);
        Managers.CONFIG.saveJson(DATA_FILE_PATH, data);
    }
    public void loadFriends() {
        Path parentDir = DATA_FILE_PATH.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (Exception ignore) {}
        }
        Map<String, Object> data = Managers.CONFIG.readJson(DATA_FILE_PATH);
        friends = toLinked((ArrayList<String>) data.getOrDefault("friends", new ArrayList<>()));
        disables = new LinkedList<>((ArrayList<String>) data.getOrDefault("disables", new ArrayList<>()));
    }

    public <T> LinkedList<T> toLinked(ArrayList<T> list) {
        return new LinkedList<>(list);
    }
}
