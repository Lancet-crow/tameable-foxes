package lancet_.tameable_foxes.mixin.fox_goals;

import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.MappingUtil;
import lancet_.tameable_foxes.ReflectionUtil;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.passive.FoxEntity$MateGoal")
public class FoxMateGoal {

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FoxEntity;addTrustedUuid(Ljava/util/UUID;)V", shift = At.Shift.AFTER))
    private void injected(CallbackInfo ci, @Local FoxEntity foxEntity) {
        PlayerEntity futureOwner;
        AnimalMateGoal mateGoal = (AnimalMateGoal) (Object) this;
        PlayerEntity thisFoxOwner = (PlayerEntity) ((TameableEntity) mateGoal.animal).getOwner();
        PlayerEntity otherFoxOwner = (PlayerEntity) ((TameableEntity) mateGoal.mate).getOwner();
        PlayerEntity lovingPlayer = mateGoal.mate.getLovingPlayer() != null ? mateGoal.mate.getLovingPlayer() : mateGoal.animal.getLovingPlayer();
        if (thisFoxOwner == null){
            if (otherFoxOwner == null){
                futureOwner = lovingPlayer;
            }
            else{
                futureOwner = otherFoxOwner;
            }
        }
        else{
            futureOwner = thisFoxOwner;
        }
        ReflectionUtil.invoke(foxEntity, "net.minecraft.class_1321",
                "method_6170",
                "(%s)V".formatted(MappingUtil.definitionToIntermediaryBytecodeName(PlayerEntity.class)),
                void.class, new Class[]{PlayerEntity.class}, futureOwner);
    }
}
