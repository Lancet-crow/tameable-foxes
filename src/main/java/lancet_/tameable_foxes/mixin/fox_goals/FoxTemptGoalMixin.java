package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public abstract class FoxTemptGoalMixin {
    @Shadow
    @Final
    protected PathAwareEntity mob;

    @Shadow protected abstract boolean isTemptedBy(LivingEntity entity);

    @Shadow @Nullable protected PlayerEntity closestPlayer;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopIfTamed(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof FoxEntity fox && ((TameableEntity) (Object) fox).isTamed()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "canStart", at = @At("RETURN"))
    private boolean notStartIfConfigIsOff(boolean original) {
        if (this.mob instanceof FoxEntity){
            return original && TameableFoxesConfig.config.untamedFoxesCanBeTempted;
        }
        return original;
    }

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopWhenSatOrConfigIsOff(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof FoxEntity fox){
            if (fox.isSitting() || ((TameableEntity) (Object) fox).isTamed()) {
                cir.setReturnValue(false);
            }
            if (!TameableFoxesConfig.config.untamedFoxesCanBeTempted) {
                cir.setReturnValue(false);
            }
            if (!isTemptedBy(this.closestPlayer)){
                cir.setReturnValue(false);
            }
        }
    }

    @ModifyReceiver(method = "isTemptedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/Ingredient;test(Lnet/minecraft/item/ItemStack;)Z"))
    private Ingredient updateTemptingStacks(Ingredient instance, ItemStack itemStack){
        if (this.mob instanceof FoxEntity){
            return Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks());
        }
        return instance;
    }
}
