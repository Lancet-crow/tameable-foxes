package lancet_.tameable_foxes.mixin.fox_goals;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OwnerHurtByTargetGoal.class)
public abstract class FoxDefendOwnerGoalMixin extends TargetGoal {

    @Shadow
    @Final
    private TamableAnimal tameAnimal;

    public FoxDefendOwnerGoalMixin(Mob mob, boolean bl, boolean bl2) {
        super(mob, bl, bl2);
    }

    @Inject(method = "canUse",
            at = @At("HEAD"), cancellable = true)
    private void checkIfOwnerWasAttacked(CallbackInfoReturnable<Boolean> cir) {
        if (tameAnimal.getType() == EntityType.FOX && tameAnimal.isTame() && !TameableFoxesConfig.config.foxesAttackWithOwner) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (tameAnimal.getType() == EntityType.FOX && tameAnimal.isTame() && !TameableFoxesConfig.config.foxesAttackWithOwner){
            return false;
        }
        return super.canContinueToUse();
    }
}
