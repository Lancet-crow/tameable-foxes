package lancet_.tameable_foxes.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import lancet_.tameable_foxes.TameableFoxes;

@Modmenu(modId = TameableFoxes.MOD_ID)
@Config(name=TameableFoxes.MOD_ID, wrapperName = "TameableFoxesConfig")
public class TameableFoxesConfigModel {
    public boolean foxesTameDirectly = true;
}
