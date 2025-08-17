package lancet_.tameable_foxes.mixin.fox_goals;


import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoxEntity.MoveToHuntGoal.class, priority = 1001)
public class FoxMoveToHuntGoalMixin {
    @Shadow
    @Final
    FoxEntity field_17995;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir) {
        if (((TameableEntity) (Object) field_17995).isTamed()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
