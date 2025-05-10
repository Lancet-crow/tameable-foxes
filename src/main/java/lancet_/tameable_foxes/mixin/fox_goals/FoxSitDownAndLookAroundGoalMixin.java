package lancet_.tameable_foxes.mixin.fox_goals;

import lancet_.tameable_foxes.ReflectionUtil;
import net.minecraft.entity.passive.FoxEntity;
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
        cir.setReturnValue(cir.getReturnValue() && ! ReflectionUtil.invoke(this.field_17986, "net.minecraft.class_1321", "method_6181", "()Z", boolean.class, new Class[0]));
    }
}
