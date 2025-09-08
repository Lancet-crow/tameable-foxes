package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public abstract class FoxTrackTargetGoal {
    @Shadow
    @Final
    protected MobEntity mob;
    @Shadow
    @Nullable
    protected LivingEntity target;

    @Shadow
    protected abstract boolean canTrack(@Nullable LivingEntity target, TargetPredicate targetPredicate);

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void checkIfFoxAndStopIfCant(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof FoxEntity) {
            if (!canTrack(this.target, TargetPredicate.DEFAULT)) {
                cir.setReturnValue(false);
            }
        }
    }
}
