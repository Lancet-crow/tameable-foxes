package lancet_.tameable_foxes;


import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.util.Optional;
import java.util.UUID;

public interface TameableTricksInterface {
    GoalSelector tameable_foxes$getFoxGoalSelector();

    boolean tameable_foxes$isBegging();

    void tameable_foxes$setBegging(boolean begging);

    boolean canAttackWithOwner(LivingEntity target, LivingEntity owner);

    TamableAnimal getTame();

    EntityDataAccessor<Optional<UUID>> getOwnerTrackedData();
}
