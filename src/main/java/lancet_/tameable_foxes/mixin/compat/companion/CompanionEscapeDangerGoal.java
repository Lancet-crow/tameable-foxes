package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.TamedFox;
import lancet_.tameable_foxes.fox_goals.FoxAttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static snownee.companion.Hooks.isInjured;

@Mixin(EscapeDangerGoal.class)
public class CompanionEscapeDangerGoal {
    @Shadow @Final protected PathAwareEntity mob;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void stopFoxRunningIfAttackingAndNotInjured(CallbackInfoReturnable<Boolean> cir){
        if (this.mob instanceof FoxEntity fox){
            if (fox.goalSelector.getRunningGoals().anyMatch(
                    (goal) -> goal.getGoal() instanceof FoxAttackWithOwnerGoal)
                    || isInjured(fox)){
                cir.setReturnValue(false);
            }
        }
    }
}
