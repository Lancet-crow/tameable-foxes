package lancet_.tameable_foxes;

import lancet_.tameable_foxes.config.TameableFoxesConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TameableFoxes.MOD_ID)
public class TameableFoxes
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "tameablefoxes";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TameableFoxesConfig CONFIG = TameableFoxesConfig.createAndLoad();

    public TameableFoxes()
    {

    }
}
