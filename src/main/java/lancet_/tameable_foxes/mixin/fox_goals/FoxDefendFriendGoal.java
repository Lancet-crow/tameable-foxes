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
    @Shadow(aliases = "field_17965")
    @Final
    Fox fox;

    @Shadow
    private @Nullable LivingEntity trustedLastHurt;

    @Shadow
    private @Nullable LivingEntity trustedLastHurtBy;

    @Shadow
    private int timestamp;

    public FoxDefendFriendGoal(Mob mob, Class<LivingEntity> targetClass, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, checkVisibility, targetPredicate);
    }

    @Inject(method = "canUse",
            at = @At("HEAD"), cancellable = true)
    private void checkIfOwnerWasAttacked(CallbackInfoReturnable<Boolean> cir) {
        TamableAnimal tame = (TamableAnimal) (Object) fox;
        assert tame != null;
        if (!TameableFoxesConfig.config.foxesAttackWithOwner) {
            cir.setReturnValue(false);
            return;
        }
        if (tame.isTame() && tame.getOwner() != null) {
            this.trustedLastHurt = tame.getOwner();
            this.trustedLastHurtBy = this.trustedLastHurt.getLastHurtByMob();
            if (this.trustedLastHurt.getLastHurtByMobTimestamp() != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions)) {
                cir.setReturnValue(true);
            }
        }
    }
}
