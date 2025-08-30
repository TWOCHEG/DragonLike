package pon.main;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import pon.main.managers.Managers;
import pon.main.managers.RotationManager;
import pon.main.managers.ServerManager;
import pon.main.modules.ModuleManager;

import pon.main.modules.misc.*;
import pon.main.modules.ui.*;
import pon.main.modules.world.*;
import pon.main.modules.client.*;

public class Main implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager MODULE_MANAGER = null;
	public static Managers managers = new Managers();
    public static final String VERSION = "0.0.1";

	public static List<Integer> cancelButtons = java.util.List.of(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_DELETE
	);

	public enum Categories {
		combat,
		ui,
		world,
		example,
        render,
        client,
        misc
	}

	@Override
	public void onInitialize() {
		EVENT_BUS.registerLambdaFactory(
			"pon.main",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);
		Managers.init();

		MODULE_MANAGER = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser(),
            new LagNotifier(),
            new DiscordRPC()
		);
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
