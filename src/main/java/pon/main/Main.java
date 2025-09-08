package pon.main;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import pon.main.managers.Managers;
import pon.main.modules.ModuleManager;

import pon.main.modules.hud.Hud;
import pon.main.modules.misc.*;
import pon.main.modules.client.*;
import pon.main.modules.render.*;
import pon.main.modules.world.*;

public class Main implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager MODULE_MANAGER = null;
    public static final String VERSION = "0.0.1";

	public static List<Integer> cancelButtons = java.util.List.of(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_DELETE
	);

	public enum Categories {
		combat,
        client,
		world,
		example,
        render,
        misc
	}

	@Override
	public void onInitialize() {
		EVENT_BUS.registerLambdaFactory(
			"pon.main",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);
		Managers.init();
        Managers.subscribe();

		MODULE_MANAGER = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser(),
            new LagNotifier(),
            new DiscordPresence(),
            new Rotations(),
            new FreeCam(),
            new Hud()
		);
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
