package lancet_.tameable_foxes.mixin.fox_goals;

import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Fox;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Fox.DefendTrustedTargetGoal.class)
public abstract class FoxDefendFriendGoal extends NearestAttackableTargetGoal<LivingEntity> {
    @Shadow
    private @Nullable LivingEntity trustedLastHurtBy;

    @Shadow
    private @Nullable LivingEntity trustedLastHurt;

    @Shadow
    private int timestamp;

    @Shadow @Final
    Fox this$0;

    public FoxDefendFriendGoal(Mob mob, Class<LivingEntity> targetClass, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, checkVisibility, targetPredicate);
    }

    @Inject(method = "canUse",
            at = @At("HEAD"), cancellable = true)
    private void checkIfOwnerWasAttacked(CallbackInfoReturnable<Boolean> cir) {
        TamableAnimal tame = (TamableAnimal) (Object) this$0;
        assert tame != null;
        if (!TameableFoxesConfig.config.foxesAttackWithOwner) {
            cir.setReturnValue(false);
            return;
        }
        if (tame.isTame() && tame.getOwner() != null) {
            this.trustedLastHurtBy = tame.getOwner();
            this.trustedLastHurt = this.trustedLastHurtBy.getLastHurtByMob();
            if (this.trustedLastHurtBy.getLastHurtByMobTimestamp() != this.timestamp && this.canAttack(this.trustedLastHurt, this.targetConditions)) {
                cir.setReturnValue(true);
            }
        }
    }
}
