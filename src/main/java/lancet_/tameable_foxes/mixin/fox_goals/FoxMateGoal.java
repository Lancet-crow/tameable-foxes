package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

import static net.minecraft.world.entity.TamableAnimal.DATA_OWNERUUID_ID;

@Mixin(Fox.FoxBreedGoal.class)
public class FoxMateGoal {
    @WrapOperation(method = "breed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Fox;addTrustedUUID(Ljava/util/UUID;)V"))
    private void checkConfigValue(Fox instance, UUID uuid, Operation<Void> original) {
        if (TameableFoxesConfig.config.foxesTrustOnBorn) {
            original.call(instance, uuid);
            TameableTricksInterface ttInterface = ((TameableTricksInterface) (instance));
            if (instance.getEntityData().get(DATA_OWNERUUID_ID).isEmpty() && instance.getEntityData().get(ttInterface.getOwnerTrackedData()).isPresent()) {
                ttInterface.getTame().setTame(true, true);
                ttInterface.getTame().setOwnerUUID(instance.getEntityData().get(ttInterface.getOwnerTrackedData()).orElse(null));
            }
        }
    }
}
