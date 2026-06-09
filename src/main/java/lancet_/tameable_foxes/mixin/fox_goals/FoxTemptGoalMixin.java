package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
    protected PathfinderMob mob;

    @Shadow protected abstract boolean shouldFollow(LivingEntity entity);

    @Shadow @Nullable protected Player player;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopIfTamed(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Fox fox && ((TameableTricksInterface) fox).getTame().isTame()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean notStartIfConfigIsOff(boolean original) {
        if (this.mob instanceof Fox){
            return original && TameableFoxesConfig.config.untamedFoxesCanBeTempted;
        }
        return original;
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void foxShouldStopWhenSatOrConfigIsOff(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Fox fox){
            if (fox.isSitting() || ((TameableTricksInterface)fox).getTame().isTame()) {
                cir.setReturnValue(false);
            }
            if (!TameableFoxesConfig.config.untamedFoxesCanBeTempted) {
                cir.setReturnValue(false);
            }
            if (!shouldFollow(this.player)){
                cir.setReturnValue(false);
            }
        }
    }

    @ModifyReceiver(method = "shouldFollow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z"))
    private Ingredient updateTemptingStacks(Ingredient instance, ItemStack itemStack){
        if (this.mob instanceof Fox){
            return Ingredient.of(TameableFoxesConfig.getFoxTemptingItemStacks());
        }
        return instance;
    }
}
