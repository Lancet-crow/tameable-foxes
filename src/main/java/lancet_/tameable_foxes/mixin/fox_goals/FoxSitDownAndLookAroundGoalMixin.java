package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.SitDownAndLookAroundGoal.class)
public class FoxSitDownAndLookAroundGoalMixin {
    @Shadow @Final
    FoxEntity field_17986;

    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Boolean> cir) {
        TameableEntity tamedFox = ((TameableEntity) (Object) field_17986);
        assert tamedFox != null;
        cir.setReturnValue(cir.getReturnValue() && !tamedFox.isTamed());
    }
}
