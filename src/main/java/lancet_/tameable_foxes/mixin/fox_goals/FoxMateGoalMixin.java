package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.UUID;

@Mixin(FoxEntity.MateGoal.class)
public class FoxMateGoalMixin {
    @WrapOperation(method = "breed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/FoxEntity;addTrustedUuid(Ljava/util/UUID;)V"))
    private void checkConfigValue(FoxEntity instance, UUID uuid, Operation<Void> original){
        Objects.requireNonNull(instance.getEntityWorld().getServer()).sendMessage(
                Text.of(String.valueOf(TameableFoxesConfig.config.foxesTrustOnBorn)));
        if (TameableFoxesConfig.config.foxesTrustOnBorn) {
            original.call(instance, uuid);
        }
    }
}
