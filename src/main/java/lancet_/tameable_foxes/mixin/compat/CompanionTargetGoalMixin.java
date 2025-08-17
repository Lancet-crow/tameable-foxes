package lancet_.tameable_foxes.mixin.compat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.companion.Hooks;

@Mixin(TrackTargetGoal.class)
public class CompanionTargetGoalMixin {

    @Final
    @Shadow
    protected MobEntity mob;
    @Shadow
    protected LivingEntity target;

    @Inject(
            at = {@At("HEAD")},
            method = {"shouldContinue"},
            cancellable = true
    )
    private void tameableFoxes_canContinueToUse(CallbackInfoReturnable<Boolean> ci) {
        if (this.mob != null) {
            LivingEntity var3 = this.target;
            if (var3 instanceof FoxEntity fox) {
                TameableEntity pet = (TameableEntity) (Object) fox;
                if (!Hooks.wantsToAttack(pet, this.mob)) {
                    ((Angerable) fox).stopAnger();
                    ci.setReturnValue(false);
                }
            }
        }

    }
}
