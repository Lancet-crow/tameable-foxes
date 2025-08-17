package lancet_.tameable_foxes.mixin.wolf;

import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Predicate;

@Debug(export = true)
@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity {

    @Final
    @Shadow
    public static Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE;

    public WolfEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArgs(method = "initGoals",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/goal/UntamedActiveTargetGoal;<init>(Lnet/minecraft/entity/passive/TameableEntity;Ljava/lang/Class;ZLjava/util/function/Predicate;)V"))
    public void modifyPredicate(Args args) {
        @Nullable Predicate<LivingEntity> targetPredicate = args.get(3);
        if (targetPredicate != null && targetPredicate.equals(FOLLOW_TAMED_PREDICATE)) {
            args.set(3, targetPredicate.and(entity -> {
                        EntityType<?> otherEntityType = entity.getType();
                        if (otherEntityType == EntityType.FOX && entity instanceof FoxEntity fox &&
                                ((TameableEntity) (Object) fox).isTamed() && !this.isTamed() &&
                                !TameableFoxesConfig.config.untamedWolvesAttackTamedFoxes) {
                            return false;
                        }
                        return true;
                    })
            );
        }
    }
}
