package lancet_.tameable_foxes.mixin.fox_goals;


import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Fox.StalkPreyGoal.class, priority = 1001)
public class FoxMoveToHuntGoalMixin {
    @Shadow
    @Final
    Fox field_17995;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (((TameableTricksInterface)field_17995).getTame().isTame()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
