package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class FoxTemptGoalMixin {
    @Shadow @Final protected PathAwareEntity mob;

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopWhenSat(CallbackInfoReturnable<Boolean> cir){
        if (this.mob instanceof FoxEntity fox && fox.isSitting()){
            cir.setReturnValue(false);
        }
    }
}
