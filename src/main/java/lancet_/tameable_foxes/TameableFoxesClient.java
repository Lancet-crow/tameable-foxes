package lancet_.tameable_foxes;

import lancet_.tameable_foxes.config.TameableFoxesConfig;
import net.fabricmc.api.ClientModInitializer;

public class TameableFoxesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TameableFoxesConfig.INSTANCE.load();
    }
}
