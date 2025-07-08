package purr.purr;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;
import purr.purr.modules.ModuleManager;

// import purr.purr.modules.render.*;
import purr.purr.modules.ui.*;
import purr.purr.modules.combat.*;
import purr.purr.modules.world.*;
import purr.purr.managers.travel.TravelChangeManager;

public class Purr implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager moduleManager = null;
	// public static final Identifier FONT = Identifier.of("purr", "font/Robloxian-UltraBold");

	@Override
	public void onInitialize() {
		EVENT_BUS.registerLambdaFactory(
			"purr.purr",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		new TravelChangeManager();

		moduleManager = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new KillAura(),
			new ConfigMenu(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser()
		);
	}
}
// тут был Егорушка 
