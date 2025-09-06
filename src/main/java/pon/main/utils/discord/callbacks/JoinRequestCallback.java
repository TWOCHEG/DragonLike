package pon.main.utils.discord.callbacks;

import com.sun.jna.Callback;
import pon.main.utils.discord.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
