package lancet_.tameable_foxes.mixin.compat.companion;

import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static snownee.companion.Hooks.isInjured;

@Mixin(WanderAroundGoal.class)
public class CompanionWanderAroundMixin {
    @Shadow @Final protected PathAwareEntity mob;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void stopIfInjured(CallbackInfoReturnable<Boolean> cir){
        if (this.mob instanceof FoxEntity fox && isInjured(fox)){
            cir.setReturnValue(false);
        }
    }
}
