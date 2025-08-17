package lancet_.tameable_foxes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.TameableEntity;

import java.util.Optional;
import java.util.UUID;

public interface TameableTricksInterface extends Angerable {
    GoalSelector getFoxGoalSelector();

    boolean isBegging();

    void setBegging(boolean begging);

    boolean canAttackWithOwner(LivingEntity target, LivingEntity owner);

    TameableEntity getTame();

    TrackedData<Optional<UUID>> getOwnerTrackedData();
}
