package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class FoxTemptGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopIfTamed(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Fox fox && ((TamableAnimal) (Object) fox).isTame()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean notStartIfConfigIsOff(boolean original) {
        return original && TameableFoxesConfig.config.untamedFoxesCanBeTempted;
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopWhenSatOrConfigIsOff(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Fox fox && (fox.isSitting() || ((TamableAnimal) (Object) fox).isTame())) {
            cir.setReturnValue(false);
        }
        if (!TameableFoxesConfig.config.untamedFoxesCanBeTempted) {
            cir.setReturnValue(false);
        }
    }
}
