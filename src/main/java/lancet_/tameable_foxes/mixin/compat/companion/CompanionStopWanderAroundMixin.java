package lancet_.tameable_foxes.mixin.compat.companion;

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static snownee.companion.Hooks.isInjured;

@Mixin(FoxEntity.StopWanderingGoal.class)
public class CompanionStopWanderAroundMixin {
    @Shadow @Final
    FoxEntity field_17970;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void dontStopIfInjured(CallbackInfoReturnable<Boolean> cir){
        if (isInjured(this.field_17970)){
            cir.setReturnValue(false);
        }
    }
}
