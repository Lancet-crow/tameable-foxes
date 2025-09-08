package lancet_.tameable_foxes.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;

public class FoxStalkPreyGoal extends Goal {
    Fox fox;
    public FoxStalkPreyGoal(Fox fox) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.fox = fox;
    }

    @Override
    public boolean canUse() {
        if (fox.isSleeping()) {
            return false;
        } else {
            LivingEntity livingentity = fox.getTarget();
            return livingentity != null
                    && livingentity.isAlive()
                    && Fox.STALKABLE_PREY.test(livingentity)
                    && fox.distanceToSqr(livingentity) > 36.0
                    && !fox.isCrouching()
                    && !fox.isInterested()
                    && !fox.jumping;
        }
    }

    @Override
    public void start() {
        fox.setSitting(false);
        fox.setFaceplanted(false);
    }

    @Override
    public void stop() {
        LivingEntity livingentity = fox.getTarget();
        if (livingentity != null && Fox.isPathClear(fox, livingentity)) {
            fox.setIsInterested(true);
            fox.setIsCrouching(true);
            fox.getNavigation().stop();
            fox.getLookControl().setLookAt(livingentity, (float)fox.getMaxHeadYRot(), (float)fox.getMaxHeadXRot());
        } else {
            fox.setIsInterested(false);
            fox.setIsCrouching(false);
        }
    }

    @Override
    public void tick() {
        LivingEntity livingentity = fox.getTarget();
        if (livingentity != null) {
            fox.getLookControl().setLookAt(livingentity, (float)fox.getMaxHeadYRot(), (float)fox.getMaxHeadXRot());
            if (fox.distanceToSqr(livingentity) <= 36.0) {
                fox.setIsInterested(true);
                fox.setIsCrouching(true);
                fox.getNavigation().stop();
            } else {
                fox.getNavigation().moveTo(livingentity, 1.5);
            }
        }
    }
}
