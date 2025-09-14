package pon.main;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import pon.main.managers.Core;
import pon.main.managers.Managers;
import pon.main.modules.client.*;

public class Main implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
    public static final String VERSION = "0.0.1";
    public static final String NAME_SPACE = "main";

    public static float TICK_TIMER = 1f;

    public static Core core = new Core();

	public static List<Integer> cancelButtons = java.util.List.of(
        GLFW.GLFW_KEY_ESCAPE,
        GLFW.GLFW_KEY_DELETE
	);

	public enum Categories {
		combat,
        client,
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
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
