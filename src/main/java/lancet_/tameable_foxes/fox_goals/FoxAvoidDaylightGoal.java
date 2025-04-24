package lancet_.tameable_foxes.fox_goals;

import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class FoxAvoidDaylightGoal extends EscapeSunlightGoal {
    private int timer = toGoalTicks(100);
    FoxEntity fox;

    public FoxAvoidDaylightGoal(PathAwareEntity mob, double speed) {
        super(mob, speed);
        fox = (FoxEntity) mob;
    }

    @Override
    public boolean canStart() {
        if (!fox.isSleeping() && !fox.isSitting() && this.mob.getTarget() == null) {
            if (fox.getWorld().isThundering() && fox.getWorld().isSkyVisible(this.mob.getBlockPos())) {
                return this.targetShadedPos();
            } else if (this.timer > 0) {
                this.timer--;
                return false;
            } else {
                this.timer = 100;
                BlockPos blockPos = this.mob.getBlockPos();
                return fox.getWorld().isDay()
                        && fox.getWorld().isSkyVisible(blockPos)
                        && !((ServerWorld)fox.getWorld()).isNearOccupiedPointOfInterest(blockPos)
                        && this.targetShadedPos();
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        fox.stopActions();
        super.start();
    }
}
