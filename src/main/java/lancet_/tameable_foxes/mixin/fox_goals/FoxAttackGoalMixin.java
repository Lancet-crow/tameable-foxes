package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoxEntity.AttackGoal.class)
public class FoxAttackGoalMixin {
    @ModifyExpressionValue(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FoxEntity;isWalking()Z"))
    private boolean canStartIfWalking(boolean original) {
        return false;
    }
}
