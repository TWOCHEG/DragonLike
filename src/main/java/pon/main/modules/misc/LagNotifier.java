package pon.main.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import pon.main.Main;
import pon.main.events.impl.EventTick;
import pon.main.events.impl.PacketEvent;
import pon.main.modules.Parent;
import pon.main.modules.settings.Setting;
import pon.main.modules.ui.Notify;
import pon.main.utils.math.Timer;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class LagNotifier extends Parent {
    private final Setting<Boolean> rubberbandNotify = new Setting<>("rubberband", true);
    private final Setting<Boolean> serverResponseNotify = new Setting<>("server tesponse", true);
    private final Setting<Integer> responseTreshold = new Setting<>("response treshold", 5, 0, 15).visibleIf(v -> serverResponseNotify.getValue());
    private final Setting<Boolean> tpsNotify = new Setting<>("TPS", true);

    private Timer notifyTimer = new Timer();
    private Timer rubberbandTimer = new Timer();
    private Timer packetTimer = new Timer();

    private boolean isRubberband = false;
    private boolean isLagging = false;

    public LagNotifier() {
        super("lag notifier", Main.Categories.misc);
    }

    @Override
    public void onEnable() {
        notifyTimer = new Timer();
        rubberbandTimer = new Timer();
        packetTimer = new Timer();
        isLagging = false;

        super.onEnable();
    }

    @EventHandler
    private void onTick(EventTick e) {
        if (!rubberbandTimer.passedMs(5000)) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            if (!isRubberband && rubberbandNotify.getValue()) {
                Notify.NotifyData n = new Notify.NotifyData("", Notify.NotifyType.Important)
                        .setNotifyTextProvider(() -> ("rubberband detected! " + decimalFormat.format((5000f - (float) rubberbandTimer.getTimeMs()) / 1000f)))
                        .setReverseProvider(() -> (5000f - rubberbandTimer.getTimeMs()) / 1000f < 0);
                n.colors.add(new Color(255, 0, 0).getRGB());
                n.colors.add(new Color(255, 255, 0).getRGB());
                n.liveTime = 100;

                notify(n);
            }
            isRubberband = true;
        } else {
            isRubberband = false;
        }

        if (packetTimer.passedMs(responseTreshold.getValue() * 1000L) && serverResponseNotify.getValue()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");

            if (!isLagging) {
                Notify.NotifyData n = new Notify.NotifyData("", Notify.NotifyType.Important)
                        .setNotifyTextProvider(() -> ("the server stopped responding " + decimalFormat.format((float) packetTimer.getTimeMs() / 1000f)))
                        .setReverseProvider(() -> (isLagging));
                n.colors.add(new Color(255, 0, 0).getRGB());
                n.colors.add(new Color(255, 255, 0).getRGB());
                n.liveTime = 100;
            }
        }

        if (Main.SERVER_MANAGER.getTPS() < 10 && notifyTimer.passedMs(60000) && tpsNotify.getValue()) {
            if (!isLagging) {
                Notify.NotifyData n = new Notify.NotifyData(
                        "! server TPS is below 10 !",
                        Notify.NotifyType.Important
                );
                n.colors.add(new Color(255, 0, 0).getRGB());
                n.colors.add(new Color(255, 255, 0).getRGB());
                n.liveTime = 100;
                notify(n);
            }

            isLagging = true;
            notifyTimer.reset();
        }

        if (Main.SERVER_MANAGER.getTPS() > 15 && isLagging) {
            notify(new Notify.NotifyData(
                    "server TPS has stabilized!",
                    Notify.NotifyType.Important
            ));
            isLagging = false;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) rubberbandTimer.reset();
        if (e.getPacket() instanceof WorldTimeUpdateS2CPacket) packetTimer.reset();
    }
}
