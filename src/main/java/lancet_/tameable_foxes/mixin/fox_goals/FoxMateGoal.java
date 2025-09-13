package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

import static net.minecraft.entity.passive.TameableEntity.OWNER_UUID;

@Mixin(FoxEntity.MateGoal.class)
public class FoxMateGoal {
    @WrapOperation(method = "breed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/FoxEntity;addTrustedUuid(Ljava/util/UUID;)V"))
    private void checkConfigValue(FoxEntity instance, UUID uuid, Operation<Void> original) {
        if (TameableFoxesConfig.config.foxesTrustOnBorn) {
            original.call(instance, uuid);
            TameableTricksInterface ttInterface = ((TameableTricksInterface) (instance));
            if (instance.getDataTracker().get(OWNER_UUID).isEmpty() && instance.getDataTracker().get(ttInterface.getOwnerTrackedData()).isPresent()) {
                ttInterface.getTame().setTamed(true);
                ttInterface.getTame().setOwnerUuid(instance.getDataTracker().get(ttInterface.getOwnerTrackedData()).orElse(null));
            }
        }
    }
}
