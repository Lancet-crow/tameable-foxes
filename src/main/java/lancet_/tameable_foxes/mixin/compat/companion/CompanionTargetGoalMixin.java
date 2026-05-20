package lancet_.tameable_foxes.mixin.compat.companion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.companion.Hooks;

@Mixin(TargetGoal.class)
public class CompanionTargetGoalMixin {

    @Final
    @Shadow
    protected Mob mob;
    @Shadow
    protected LivingEntity targetMob;

    @Inject(
            at = {@At("HEAD")},
            method = {"canContinueToUse"},
            cancellable = true
    )
    private void tameableFoxes_canContinueToUse(CallbackInfoReturnable<Boolean> ci) {
        if (this.mob != null) {
            LivingEntity var3 = this.targetMob;
            if (var3 instanceof Fox fox) {
                TamableAnimal pet = (TamableAnimal) (Object) fox;
                if (!Hooks.wantsToAttack(pet, this.mob)) {
                    ((NeutralMob) fox).stopBeingAngry();
                    ci.setReturnValue(false);
                }
            }
        }

    }
}
