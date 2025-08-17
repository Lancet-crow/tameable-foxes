package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin implements TameableTricksInterface {

    @Inject(method = "canAttackWithOwner", at = @At("RETURN"), cancellable = true)
    public void fox_wantsToAttack(LivingEntity target, LivingEntity owner, CallbackInfoReturnable<Boolean> cir) {
        if ((AnimalEntity) (Object) this instanceof FoxEntity){
            cir.setReturnValue(cir.getReturnValue() && TameableFoxesConfig.config.foxesAttackWithOwner);
        }
    }
}
