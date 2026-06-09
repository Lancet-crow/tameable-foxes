package lancet_.tameable_foxes.mixin.wolf;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Predicate;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin extends TamableAnimal {

    @Final
    @Shadow
    public static Predicate<LivingEntity> PREY_SELECTOR;

    public WolfEntityMixin(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyArgs(method = "registerGoals",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/target/NonTameRandomTargetGoal;<init>(Lnet/minecraft/world/entity/TamableAnimal;Ljava/lang/Class;ZLjava/util/function/Predicate;)V"))
    public void modifyPredicate(Args args) {
        @Nullable Predicate<LivingEntity> targetPredicate = args.get(3);
        if (targetPredicate != null && targetPredicate.equals(PREY_SELECTOR)) {
            args.set(3, targetPredicate.and(entity -> !(entity instanceof Fox fox) ||
                    !((TameableTricksInterface) fox).getTame().isTame() || this.isTame() ||
                    TameableFoxesConfig.config.untamedWolvesAttackTamedFoxes)
            );
        }
    }
}
