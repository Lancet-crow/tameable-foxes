package lancet_.tameable_foxes;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TameableFoxes.MODID)
public class TameableFoxes
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tameable_foxes";
    public static final Logger LOGGER = LogUtils.getLogger();
    public TameableFoxes()
    {

    }
}
