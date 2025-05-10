package lancet_.tameable_foxes;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableFoxes implements ModInitializer {
	public static final String MOD_ID = "tameable-foxes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TameableFoxesConfig.init();

		LOGGER.info("Foxes becoming tameable...");
	}
}