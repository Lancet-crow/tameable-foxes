package lancet_.tameable_foxes.mixin.compat;

import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
    private static void stopFoxFromFleeingAfterTeleport(MobEntity entity, World level, BlockPos blockPos, @Nullable Boolean canFly, @Nullable Entity avoidColliding, CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (cir.getReturnValue().isPresent()) {
            if (entity instanceof FoxEntity fox) {
                ((TameableTricksInterface) fox).getFoxGoalSelector().getGoals().forEach(goal -> {
                    if (goal.getGoal() instanceof FleeEntityGoal<?>) {
                        goal.getGoal().stop();
                    } else if (goal.getGoal() instanceof EscapeDangerGoal escGoal) {
                        fox.setAttacker(null);
                        escGoal.stop();
                    }
                });
            }
        }
    }
}
