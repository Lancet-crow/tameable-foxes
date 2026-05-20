package lancet_.tameable_foxes.mixin.compat.ecologics;

import com.bawnorton.mixinsquared.TargetHandler;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(value = Fox.class, priority = 1001)
public class EcologicsFoxMixin {
    @Unique
    @TargetHandler(
            mixin="samebutdifferent.ecologics.mixin.FoxMixin",
            name="registerGoals"
    )
    @ModifyArg(method = "@MixinSquared:Handler",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/target/NearestAttackableTargetGoal;<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V"), index = 5)
    private Predicate<LivingEntity> tameableFoxes$fixEcologics(Predicate<LivingEntity> predicate){
        return (target) -> !(((TameableTricksInterface)this).getTame().isTame()) && predicate.test(target);
    }
}
