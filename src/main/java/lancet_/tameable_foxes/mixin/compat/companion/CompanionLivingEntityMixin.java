package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.fox_goals.FoxFollowPlayerGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.companion.CompanionCommonConfig;
import snownee.companion.CompanionTamableAnimal;

@Mixin(LivingEntity.class)
public class CompanionLivingEntityMixin {
    @Inject(at = @At("TAIL"), method = "damage")
    private void companion_hurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> ci) {
        if (CompanionCommonConfig.petTeleportToOwnerWhenInjured && !damageSource.isOf(DamageTypes.OUT_OF_WORLD)
                && ((Entity) (Object) this).getType().equals(EntityType.FOX)) {
            ((CompanionTamableAnimal) this).companion$tryTeleportToOwner(damageSource);
            ((FoxEntity) (Object) this).goalSelector.getGoals().forEach((goal) -> {
                if (goal.getGoal() instanceof FoxFollowPlayerGoal followPlayerGoal){
                    followPlayerGoal.start();
                }
            });
        }
    }
}
