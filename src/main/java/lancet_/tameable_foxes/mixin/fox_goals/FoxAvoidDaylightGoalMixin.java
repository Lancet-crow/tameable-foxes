package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Fox.SeekShelterGoal.class)
public class FoxAvoidDaylightGoalMixin {

    @Shadow @Final
    Fox this$0;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (this.this$0.isSitting()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
