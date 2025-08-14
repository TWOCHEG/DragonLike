package pon.purr;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;

import net.fabricmc.loader.api.FabricLoader;
import pon.purr.modules.ModuleManager;

import pon.purr.modules.ui.*;
import pon.purr.modules.world.*;
import pon.purr.modules.render.*;

public class Purr implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager moduleManager = null;

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
			"pon.purr",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		moduleManager = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser(),
            new FreeCam()
		);
	}

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }
}
// тут был Егорушка 
