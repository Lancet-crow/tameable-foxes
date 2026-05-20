package lancet_.tameable_foxes.mixin.compat.ecologics;

import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import samebutdifferent.ecologics.entity.Squirrel;

@Mixin(Squirrel.class)
public abstract class EcologicsSquirrelMixin {

    @Redirect(method = "registerGoals",
    at = @At(value = "NEW", target = "(Lnet/minecraft/world/entity/PathfinderMob;Ljava/lang/Class;FDD)Lnet/minecraft/world/entity/ai/goal/AvoidEntityGoal;"))
    private AvoidEntityGoal<?> tameableFoxes$fixFearedSquirrels(PathfinderMob pathfinderMob, Class<Fox> foxClass, float f, double d, double e){
        return new AvoidEntityGoal<>(pathfinderMob, foxClass, f, d, e,
                (livingEntity) -> (!((TameableTricksInterface)livingEntity).getTame().isTame()));
    }
}
