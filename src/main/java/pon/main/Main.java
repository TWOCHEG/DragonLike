package pon.main;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import pon.main.managers.RotationManager;
import pon.main.managers.ServerManager;
import pon.main.modules.ModuleManager;

import pon.main.modules.misc.LagNotifier;
import pon.main.modules.ui.*;
import pon.main.modules.world.*;

public class Main implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager MODULE_MANAGER = null;
	public static RotationManager ROTATIONS = new RotationManager();
    public static ServerManager SERVER_MANAGER = new ServerManager();

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
		EVENT_BUS.subscribe(ROTATIONS);

		MODULE_MANAGER = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser(),
            new LagNotifier()
		);
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
