package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobEntityMixin {

    @WrapOperation(method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean checkFoxIgnoreMobGriefingRule(GameRules instance, GameRules.Key<GameRules.BooleanValue> rule,
                                                  Operation<Boolean> original) {
        if (((Mob) (Object) this) instanceof Fox && TameableFoxesConfig.config.foxesCanIgnoreMobGriefingRule) {
            return true;
        }
        return original.call(instance, rule);
    }
}
