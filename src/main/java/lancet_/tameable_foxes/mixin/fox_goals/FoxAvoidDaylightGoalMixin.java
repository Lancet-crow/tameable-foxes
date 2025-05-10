package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.AvoidDaylightGoal.class)
public class FoxAvoidDaylightGoalMixin {
    @Shadow @Final
    FoxEntity field_17991;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir){
        if (this.field_17991.isSitting()){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
