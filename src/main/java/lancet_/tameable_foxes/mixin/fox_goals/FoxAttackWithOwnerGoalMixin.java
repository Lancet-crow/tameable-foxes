package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttackWithOwnerGoal.class)
public class FoxAttackWithOwnerGoalMixin {
    @Shadow @Final private TameableEntity tameable;

    @Shadow private LivingEntity attacking;

    @ModifyExpressionValue(method = "canStart",
                        at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/entity/passive/TameableEntity;" +
                        "canAttackWithOwner(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean checkIfFox(boolean original, @Local LivingEntity target, @Local LivingEntity owner){
        if (((AnimalEntity) this.tameable) instanceof FoxEntity fox){
            return ((TameableTricksInterface) fox).canAttackWithOwner(target, owner);
        }
        else{
            return original;
        }
    }

    @Inject(method = "start", at = @At("TAIL"))
    private void makeFoxAggressive(CallbackInfo ci){
        if (((AnimalEntity) this.tameable) instanceof FoxEntity fox){
            ((Angerable) fox).setAngryAt(attacking.getUuid());
        }
    }
}
