package lancet_.tameable_foxes;

import lancet_.tameable_foxes.config.TameableFoxesConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableFoxes implements ModInitializer {
	public static final String MOD_ID = "tameable-foxes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TameableFoxesConfig CONFIG = TameableFoxesConfig.createAndLoad();

	@Override
	public void onInitialize() {
		LOGGER.info("Foxes becoming tameable...");
	}
}