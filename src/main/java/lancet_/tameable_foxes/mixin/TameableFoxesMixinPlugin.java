package lancet_.tameable_foxes.mixin;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** :irritatered: */
public final class TameableFoxesMixinPlugin implements IMixinConfigPlugin {
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "lancet_.tameable_foxes.mixin.compat.CalmDownDogMixin", () -> FabricLoader.getInstance().isModLoaded("calmdowndog"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionHooksMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionTargetGoalMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionFoxMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionFoxAttackGoalMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionLivingEntityMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionEscapeDangerGoal", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionStopWanderAroundMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.companion.CompanionWanderAroundMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.NEAPetAnimationMixin", () -> FabricLoader.getInstance().isModLoaded("notenoughanimations")
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    // Boilerplate

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}