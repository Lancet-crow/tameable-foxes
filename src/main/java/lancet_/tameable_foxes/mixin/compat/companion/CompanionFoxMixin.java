package lancet_.tameable_foxes.mixin.compat.companion;

import lancet_.tameable_foxes.TamedFox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import snownee.companion.CompanionTamableAnimal;
import snownee.companion.Hooks;

@Mixin(FoxEntity.class)
public abstract class CompanionFoxMixin implements CompanionTamableAnimal {
    @Unique
    private long companion$lastTeleportation = Long.MIN_VALUE;

    public void companion$tryTeleportToOwner(DamageSource damageSource) {
        FoxEntity entity = (FoxEntity) (Object) this;
        if (Hooks.isInjured(entity)) {
            LivingEntity owner = entity.getWorld().getPlayerByUuid(((TamedFox)entity).getOwnerUuid(entity));
            if (owner != damageSource.getAttacker() && Hooks.shouldFollowOwner(owner, entity)) {
                long time = entity.getWorld().getTime();
                long interval = time - this.companion$lastTeleportation;
                if (interval <= 0L || interval >= 600L) {
                    this.companion$lastTeleportation = time;
                    entity.setTarget(null);
                    Hooks.teleportWithRandomOffset(entity, owner.getWorld(), owner.getBlockPos().offset(owner.getHorizontalFacing().getOpposite(), 3), null, owner).ifPresent((vec) -> entity.requestTeleport(vec.x, vec.y, vec.z));
                }
            }
        }
    }

}
