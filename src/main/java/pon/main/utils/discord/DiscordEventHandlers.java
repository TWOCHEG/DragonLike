package pon.main.utils.discord;

import java.util.Arrays;
import java.util.List;
import pon.main.utils.discord.callbacks.JoinGameCallback;
import pon.main.utils.discord.callbacks.ErroredCallback;
import pon.main.utils.discord.callbacks.ReadyCallback;
import pon.main.utils.discord.callbacks.SpectateGameCallback;
import pon.main.utils.discord.callbacks.JoinRequestCallback;
import pon.main.utils.discord.callbacks.DisconnectedCallback;
import com.sun.jna.Structure;

public class DiscordEventHandlers extends Structure {
    public DisconnectedCallback disconnected;
    public JoinRequestCallback joinRequest;
    public SpectateGameCallback spectateGame;
    public ReadyCallback ready;
    public ErroredCallback errored;
    public JoinGameCallback joinGame;
    
    protected List<String> getFieldOrder() {
        return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
    }
}