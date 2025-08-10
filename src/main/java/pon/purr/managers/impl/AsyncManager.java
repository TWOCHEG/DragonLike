package pon.purr.managers.impl;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import pon.purr.Purr;
import pon.purr.events.impl.EventPostTick;
import pon.purr.events.impl.EventSync;
import pon.purr.events.impl.EventTick;
import pon.purr.managers.IManager;
import pon.purr.managers.Managers;
import pon.purr.modules.Parent;
import pon.purr.modules.ui.Notify;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncManager implements IManager {
    private ClientService clientService = new ClientService();
    public static ExecutorService executor = Executors.newCachedThreadPool();
    private volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();
    public final AtomicBoolean ticking = new AtomicBoolean(false);

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostTick(EventPostTick e) {
        if (mc.world == null) return;

        threadSafeEntityList = Lists.newArrayList(mc.world.getEntities());
        threadSafePlayersList = Lists.newArrayList(mc.world.getPlayers());
        ticking.set(false);
    }

    public Iterable<Entity> getAsyncEntities() {
        return threadSafeEntityList;
    }

    public List<AbstractClientPlayerEntity> getAsyncPlayers() {
        return threadSafePlayersList;
    }

    public AsyncManager() {
        clientService.setName("ThunderHack-AsyncProcessor");
        clientService.setDaemon(true);
        clientService.start();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!clientService.isAlive()) {
            clientService = new ClientService();
            clientService.setName("ThunderHack-AsyncProcessor");
            clientService.setDaemon(true);
            clientService.start();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Managers.TELEMETRY.onUpdate();
                    if (!Parent.fullNullCheck()) {
                        Purr.moduleManager.modules.forEach(m -> {
                            if (m.getEnable()) m.onThread();
                        });
                        Thread.sleep(100);
                    } else Thread.yield();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Parent.notify(new Notify.NotifyData(exception.getMessage(), Notify.NotifyType.System, Parent.getNotifyLiveTime()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(EventTick e) {
        ticking.set(true);
    }

    public void run(Runnable runnable, long delay) {
        executor.execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runnable.run();
        });
    }

    public void run(Runnable r) {
        executor.execute(r);
    }
}
