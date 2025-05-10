package lancet_.tameable_foxes.mixin.fox_goals;


import lancet_.tameable_foxes.ReflectionUtil;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.MoveToHuntGoal.class)
public class FoxMoveToHuntGoalMixin {
    @Shadow @Final
    FoxEntity field_17995;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void tameable_foxes$canStart(CallbackInfoReturnable<Boolean> cir){
        if (ReflectionUtil.invoke(this.field_17995, "net.minecraft.class_1321", "method_6181", "()Z", boolean.class, new Class[0])){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
