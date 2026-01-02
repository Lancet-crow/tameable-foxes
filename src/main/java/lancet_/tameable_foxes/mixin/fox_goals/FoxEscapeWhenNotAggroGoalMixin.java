package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Fox.FoxPanicGoal.class)
public class FoxEscapeWhenNotAggroGoalMixin {

    @Shadow @Final
    Fox this$0;

    @ModifyExpressionValue(method = "shouldPanic",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/PanicGoal;shouldPanic()Z"))
    private boolean isInDangerWithOwner(boolean original) {
        TamableAnimal pet = ((TamableAnimal) (Object) this$0);
        return original && this$0.getLastHurtByMob() != pet.getOwner()
                && (!pet.isTame() || !TameableFoxesConfig.config.foxesAttackWithOwner);
    }
}
