package pon.main.utils.discord.callbacks;

import pon.main.utils.discord.DiscordUser;
import com.sun.jna.Callback;

public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
