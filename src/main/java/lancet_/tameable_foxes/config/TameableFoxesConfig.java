package lancet_.tameable_foxes.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.ConfigInstance;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public class TameableFoxesConfig {
    public static final ConfigInstance<TameableFoxesConfig> INSTANCE = new GsonConfigInstance<>(
            TameableFoxesConfig.class,
            FabricLoader.getInstance().getConfigDir().resolve("tameable-foxes.json")
    );

    @SerialEntry
    public boolean foxesTameDirectly = false;

    public static ConfigCategory getConfigCategory() {
        TameableFoxesConfig config = INSTANCE.getConfig();
        TameableFoxesConfig defaults = INSTANCE.getDefaults();

        return ConfigCategory.createBuilder()
                .name(Text.of("Tameable Foxes"))
                .option(Option.createBuilder(boolean.class)
                        .name(Text.of("Do foxes tame directly?"))
                        .description(OptionDescription.of(Text.of("Foxes will be tamed directly. Mechanic of taming through breeding still works.")))
                        .binding(defaults.foxesTameDirectly, () -> config.foxesTameDirectly, val -> config.foxesTameDirectly = val)
                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(false))
                        .build())
                .build();
    }
}
