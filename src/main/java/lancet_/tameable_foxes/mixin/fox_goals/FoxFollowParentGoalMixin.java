package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.FollowParentGoal.class)
public class FoxFollowParentGoalMixin {

    @Final
    @Shadow
    private FoxEntity fox;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir){
        if (this.fox.isSitting()){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
