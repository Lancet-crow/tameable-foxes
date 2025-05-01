package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public abstract class MobEntityLogMixin {

    @WrapOperation(method = "tickMovement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean checkFoxIgnoreMobGriefingRule(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule,
                                                  Operation<Boolean> original){
        if (((MobEntity)(Object) this) instanceof FoxEntity && TameableFoxesConfig.config.foxesCanIgnoreMobGriefingRule){
            return true;
        }
        return original.call(instance, rule);
    }
}
