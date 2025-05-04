package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.fox_goals.FoxFollowPlayerGoal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(snownee.companion.Hooks.class)
public class CompanionHooksMixin {
    @Inject(
            method = "shouldFollowOwner",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void shouldFollowIfFox(LivingEntity owner, MobEntity pet, CallbackInfoReturnable<Boolean> cir) {
        if (pet instanceof FoxEntity animal) {
            cir.setReturnValue(!animal.isSitting());
        }
    }
}
