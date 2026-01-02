package lancet_.tameable_foxes;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.util.Optional;
import java.util.UUID;

public interface TameableTricksInterface extends NeutralMob {
    GoalSelector getFoxGoalSelector();

    boolean isBegging();

    void setBegging(boolean begging);

    boolean canAttackWithOwner(net.minecraft.world.entity.LivingEntity target, LivingEntity owner);

    TamableAnimal getTame();

    EntityDataAccessor<Optional<UUID>> getOwnerTrackedData();
}
