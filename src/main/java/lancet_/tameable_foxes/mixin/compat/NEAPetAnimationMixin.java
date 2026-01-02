package lancet_.tameable_foxes.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import dev.tr7zw.notenoughanimations.access.PlayerData;
import dev.tr7zw.notenoughanimations.animations.hands.PetAnimation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PetAnimation.class)
public class NEAPetAnimationMixin {
    @Shadow
    private Entity targetPet;

    @Inject(method = "isValid", at = @At("TAIL"), cancellable = true)
    public void allowPettingFoxes(AbstractClientPlayer entity, PlayerData data, CallbackInfoReturnable<Boolean> cir,
                                  @Local EntityHitResult entHit) {
        if (entHit != null && entHit.getEntity().getType() == EntityType.FOX) {
            Animal pet = (Animal) entHit.getEntity();
            double dif = pet.getY() - entity.getY();
            if (Math.abs(dif) < 0.6) {
                this.targetPet = pet;
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
