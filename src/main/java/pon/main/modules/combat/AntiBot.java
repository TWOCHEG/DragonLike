package pon.main.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import pon.main.Main;
import pon.main.events.impl.EventSync;
import pon.main.modules.Parent;
import pon.main.modules.client.Notify;
import pon.main.modules.settings.Setting;
import pon.main.modules.misc.FakePlayer;
import pon.main.utils.math.Timer;
import pon.main.utils.player.PlayerUtility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AntiBot extends Parent {
    public static ArrayList<PlayerEntity> bots = new ArrayList<>();
    public Setting<Boolean> onlyAura = new Setting<>("only on kill aura", true);
    private final Setting<Mode> mode = new Setting<>(Mode.UUIDCheck);
    public Setting<Integer> checkTicks = new Setting<>("check ticks", 3, 0, 10).visible(v -> mode.getValue() == Mode.MotionCheck);
    private final Timer clearTimer = new Timer();
    private int ticks = 0;
    private Map<PlayerEntity, Integer> zeroPingCounter = new HashMap<>();

    public AntiBot() {
        super("anti bot", Main.Categories.combat);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!onlyAura.getValue()) mc.world.getPlayers().forEach(this::markAsBot);
        else if (KillAura.target instanceof PlayerEntity ent) this.markAsBot(ent);

        if (clearTimer.passedMs(10000)) {
            bots.clear();
            ticks = 0;
            clearTimer.reset();
        }
    }

    private void markAsBot(PlayerEntity ent) {
        if (bots.contains(ent))
            return;

        switch (mode.getValue()) {
            case UUIDCheck -> {
                if (!ent.getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + ent.getName().getString()).getBytes(StandardCharsets.UTF_8))) && ent instanceof OtherClientPlayerEntity
                        && (FakePlayer.fakePlayer == null || ent.getId() != FakePlayer.fakePlayer.getId())
                        && !ent.getName().getString().contains("-")) {
                    this.addBot(ent);
                }
            }
            case MotionCheck -> {
                double diffX = ent.getX() - ent.lastX;
                double diffZ = ent.getZ() - ent.lastZ;

                if ((diffX * diffX) + (diffZ * diffZ) > 0.5D) {
                    if (ticks >= checkTicks.getValue())
                        this.addBot(ent);
                    ticks++;
                }
            }
            case ZeroPing -> {
                if (PlayerUtility.getEntityPing(ent) == 0) {
                    zeroPingCounter.put(ent, zeroPingCounter.getOrDefault(ent, 0));
                    if (zeroPingCounter.get(ent) > 5) {
                        addBot(ent);
                        zeroPingCounter.remove(ent);
                    }
                }
            }
        }
    }

    private void addBot(PlayerEntity entity) {
        bots.add(entity);
        notify(
            new Notify.NotifyData(
                entity.getName().getString() + " is a bot!",
                Notify.NotifyType.Important
            )
        );
    }

    public enum Mode {
        UUIDCheck, MotionCheck, ZeroPing
    }
}

