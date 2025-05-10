package lancet_.tameable_foxes.mixin.fox_goals;

import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SitGoal.class)
public class FoxSitGoalMixin {
    @Shadow @Final private TameableEntity tameable;

    @Inject(method = "start", at = @At("TAIL"))
    private void injected(CallbackInfo ci) {
        this.tameable.setMovementSpeed(0f);
    }
}
