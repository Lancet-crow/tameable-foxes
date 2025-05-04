package lancet_.tameable_foxes.mixin.compat.companion;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.compat.companion.FoxHooks;
import lancet_.tameable_foxes.fox_goals.FoxAttackWithOwnerGoal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoxAttackWithOwnerGoal.class)
public class CompanionFoxAttackGoalMixin {
    @Shadow private LivingEntity attacking;

    @Shadow @Final private FoxEntity fop;

    @WrapOperation(method = "canStart",
            at = @At(value = "INVOKE",
                    target = "Llancet_/tameable_foxes/fox_goals/FoxAttackWithOwnerGoal;canAttackWithOwner(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean checkCompanion(FoxAttackWithOwnerGoal instance, LivingEntity thisFoxUuid, LivingEntity owner, Operation<Boolean> original){
        return original.call(instance, this.attacking, owner) && FoxHooks.wantsToAttack(this.fop, this.attacking);
    }
}
