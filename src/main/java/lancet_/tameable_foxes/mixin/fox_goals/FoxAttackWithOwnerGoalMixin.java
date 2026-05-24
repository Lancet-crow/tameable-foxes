package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OwnerHurtTargetGoal.class)
public abstract class FoxAttackWithOwnerGoalMixin extends TargetGoal {
    @Shadow
    @Final
    private TamableAnimal tameAnimal;

    @Shadow
    private LivingEntity ownerLastHurt;

    public FoxAttackWithOwnerGoalMixin(Mob mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @ModifyExpressionValue(method = "canUse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/TamableAnimal;" +
                            "wantsToAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean checkIfFox(boolean original, @Local LivingEntity target, @Local LivingEntity owner) {
        if (((Animal) this.tameAnimal) instanceof Fox fox) {
            return ((TameableTricksInterface) fox).canAttackWithOwner(target, owner);
        }
        return original;
    }

    @Inject(method = "start", at = @At("TAIL"))
    private void makeFoxAggressive(CallbackInfo ci) {
        if (((Animal) this.tameAnimal) instanceof Fox fox && !TameableFoxesConfig.config.foxesAttackWithOwner) {
            ((NeutralMob) fox).setPersistentAngerTarget(ownerLastHurt.getUUID());
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (((Animal) this.tameAnimal) instanceof Fox){
            return super.canContinueToUse() && TameableFoxesConfig.config.foxesAttackWithOwner;
        }
        return super.canContinueToUse();
    }
}
