package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoxEntity.EscapeWhenNotAggressiveGoal.class)
public class FoxEscapeWhenNotAggroGoalMixin {
    @Shadow(aliases = "field_17983")
    @Final
    FoxEntity fox;

    @ModifyExpressionValue(method = "isInDanger",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/EscapeDangerGoal;isInDanger()Z"))
    private boolean isInDangerWithOwner(boolean original) {
        TameableEntity pet = ((TameableEntity) (Object) fox);
        return original && fox.getAttacker() != pet.getOwner()
                && (!pet.isTamed() || !TameableFoxesConfig.config.foxesAttackWithOwner);
    }
}
