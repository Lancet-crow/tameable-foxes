package lancet_.tameable_foxes.mixin.fox_goals;

import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(FoxEntity.DefendFriendGoal.class)
public abstract class FoxDefendFriendGoal extends ActiveTargetGoal<LivingEntity> {
    @Shadow(aliases = "field_17965")
    @Final
    FoxEntity fox;

    @Shadow
    private @Nullable LivingEntity friend;

    @Shadow
    private @Nullable LivingEntity offender;

    @Shadow
    private int lastAttackedTime;

    public FoxDefendFriendGoal(MobEntity mob, Class<LivingEntity> targetClass, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, checkVisibility, targetPredicate);
    }

    @Inject(method = "canStart",
            at = @At("HEAD"), cancellable = true)
    private void checkIfOwnerWasAttacked(CallbackInfoReturnable<Boolean> cir) {
        TameableEntity tame = (TameableEntity) (Object) fox;
        assert tame != null;
        if (!TameableFoxesConfig.config.foxesAttackWithOwner) {
            cir.setReturnValue(false);
            return;
        }
        if (tame.isTamed() && tame.getOwner() != null) {
            this.friend = tame.getOwner();
            this.offender = this.friend.getAttacker();
            if (this.friend.getLastAttackedTime() != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate)) {
                cir.setReturnValue(true);
            }
        }
    }
}
