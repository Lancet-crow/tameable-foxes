package lancet_.tameable_foxes.mixin.fox_goals;

import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.animal.Fox.FoxLookAtPlayerGoal")
public class FoxLookAtEntityGoal {
    @Shadow
    @Final
    Fox field_19261;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    public void cantStartIfFoxBegging(CallbackInfoReturnable<Boolean> cir) {
        if (((TameableTricksInterface) this.field_19261).isBegging()) {
            cir.setReturnValue(false);
        }
    }
}
