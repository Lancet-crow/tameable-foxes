package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.companion.Hooks;

import java.util.Optional;

@Mixin(Hooks.class)
public class CompanionHooksMixin {
    @Inject(method = "teleportWithRandomOffset",
            at = @At(value = "RETURN"))
    private static void stopFoxFromFleeingAfterTeleport(LivingEntity entity, Level level, BlockPos blockPos, Boolean canFly, Entity avoidColliding, CallbackInfoReturnable<Optional<Vec3>> cir) {
        if (cir.getReturnValue().isPresent()) {
            if (entity instanceof Fox fox) {
                ((TameableTricksInterface) fox).getFoxGoalSelector().getAvailableGoals().forEach(goal -> {
                    if (goal.getGoal() instanceof AvoidEntityGoal<?>) {
                        goal.getGoal().stop();
                    } else if (goal.getGoal() instanceof PanicGoal escGoal) {
                        fox.setLastHurtByMob(null);
                        escGoal.stop();
                    }
                });
            }
        }
    }
}
