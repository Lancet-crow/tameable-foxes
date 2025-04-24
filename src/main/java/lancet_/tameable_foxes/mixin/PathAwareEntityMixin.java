package lancet_.tameable_foxes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathAwareEntity.class)
public class PathAwareEntityMixin extends MobEntity {

    MobEntity mob;
    protected PathAwareEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        mob = (MobEntity) (Object) entityType;
    }

    @Inject(method = "updateLeash()V", at = @At(value="INVOKE", target = "Lnet/minecraft/entity/mob/PathAwareEntity;updateForLeashLength(F)V"), cancellable = true)
    protected void updateLeash(CallbackInfo ci) {
        mob = (MobEntity) this;
        Entity entity = mob.getHoldingEntity();
        if (mob instanceof FoxEntity && ((FoxEntity)mob).isSitting()) {
            assert entity != null;
            mob.setPositionTarget(entity.getBlockPos(), 5);
            float f = mob.distanceTo(entity);
            if (f > 10.0F) {
                mob.detachLeash(true, true);
            }
            ci.cancel();
        }
    }
}
