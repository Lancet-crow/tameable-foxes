package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Fox.FoxMeleeAttackGoal.class)
public class FoxAttackGoalMixin {
    @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;isFaceplanted()Z"))
    private boolean canStartIfWalking(boolean original) {
        return false;
    }
}
