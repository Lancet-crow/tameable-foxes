package lancet_.tameable_foxes.mixin.compat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoxEntity.class)
public abstract class CalmDownDogFoxMixin extends AnimalEntity implements Angerable {
    protected CalmDownDogFoxMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
}
