package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class FoxTemptGoalMixin {
    @Shadow @Final protected PathAwareEntity mob;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopIfTamed(CallbackInfoReturnable<Boolean> cir){
        if (this.mob instanceof FoxEntity fox && ((TameableEntity) (Object) fox).isTamed()){
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "canStart", at = @At("RETURN"))
    private boolean notStartIfConfigIsOff(boolean original){
        return original && TameableFoxesConfig.config.untamedFoxesCanBeTempted;
    }

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopWhenSatOrConfigIsOff(CallbackInfoReturnable<Boolean> cir){
        if (this.mob instanceof FoxEntity fox && (fox.isSitting() || ((TameableEntity) (Object) fox).isTamed())){
            cir.setReturnValue(false);
        }
        if (!TameableFoxesConfig.config.untamedFoxesCanBeTempted){
            cir.setReturnValue(false);
        }
    }
}
