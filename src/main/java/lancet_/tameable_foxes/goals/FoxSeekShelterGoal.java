package lancet_.tameable_foxes.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.animal.Fox;

public class FoxSeekShelterGoal extends FleeSunGoal {
    private int interval = reducedTickDelay(100);

    Fox fox;

    public FoxSeekShelterGoal(double speedModifier, Fox fox) {
        super(fox, speedModifier);
        this.fox = fox;
    }

    @Override
    public boolean canUse() {
        if (!fox.isSleeping() && !fox.isSitting() && this.mob.getTarget() == null) {
            if (fox.level().isThundering() && fox.level().canSeeSky(this.mob.blockPosition())) {
                return this.setWantedPos();
            } else if (this.interval > 0) {
                this.interval--;
                return false;
            } else {
                this.interval = 100;
                BlockPos blockpos = this.mob.blockPosition();
                return fox.level().isDay()
                        && fox.level().canSeeSky(blockpos)
                        && !((ServerLevel)fox.level()).isVillage(blockpos)
                        && this.setWantedPos();
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        fox.clearStates();
        super.start();
    }
}
