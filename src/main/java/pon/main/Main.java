package pon.main;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import pon.main.managers.RotationManager;
import pon.main.modules.ModuleManager;

import pon.main.modules.ui.*;
import pon.main.modules.world.*;

public class Main implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager moduleManager = null;
	public static RotationManager rotations = new RotationManager();

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
        client
	}

	@Override
	public void onInitialize() {
		EVENT_BUS.registerLambdaFactory(
			"pon.main",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);
		EVENT_BUS.subscribe(rotations);

		moduleManager = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser()
		);
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
