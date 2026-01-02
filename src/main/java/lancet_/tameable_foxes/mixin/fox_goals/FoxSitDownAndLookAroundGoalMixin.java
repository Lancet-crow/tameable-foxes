package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Fox.PerchAndSearchGoal.class)
public class FoxSitDownAndLookAroundGoalMixin {

    @Shadow @Final
    Fox this$0;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Boolean> cir) {
        TamableAnimal tamedFox = ((TamableAnimal) (Object) this$0);
        assert tamedFox != null;
        cir.setReturnValue(cir.getReturnValue() && !tamedFox.isTame());
    }
}
