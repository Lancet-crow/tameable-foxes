package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.TamedFox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lancet_.tameable_foxes.compat.companion.FoxHooks.wantsToAttack;

@Mixin(value = TrackTargetGoal.class, priority = 1001)
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
    private void tameable_foxes_canContinueToUse(CallbackInfoReturnable<Boolean> ci) {
        if (this.target != null && this.mob instanceof FoxEntity foxEntity && (((TamedFox)foxEntity).isTamed(foxEntity))) {
            ci.setReturnValue(wantsToAttack(foxEntity, this.target));
        }
    }
}
