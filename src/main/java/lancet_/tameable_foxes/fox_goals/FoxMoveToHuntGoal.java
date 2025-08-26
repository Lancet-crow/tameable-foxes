package lancet_.tameable_foxes.fox_goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;

import java.util.EnumSet;

import static net.minecraft.entity.passive.FoxEntity.OWNER;

public class FoxMoveToHuntGoal extends Goal {
    FoxEntity fox;
    public FoxMoveToHuntGoal(FoxEntity fox) {
        this.fox = fox;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean canStart() {
        if (fox.isSleeping() || fox.isSitting() || fox.getDataTracker().get(OWNER).orElse(null) != null) {
            return false;
        } else {
            LivingEntity livingEntity = fox.getTarget();
            return livingEntity != null && livingEntity.isAlive() && FoxEntity.CHICKEN_AND_RABBIT_FILTER.test(livingEntity) && fox.squaredDistanceTo(livingEntity) > 36.0 && !fox.isInSneakingPose() && !fox.isRollingHead() && !fox.jumping;
        }
    }

    public void start() {
        fox.setSitting(false);
        fox.setWalking(false);
    }

    public void stop() {
        LivingEntity livingEntity = fox.getTarget();
        if (livingEntity != null && FoxEntity.canJumpChase(fox, livingEntity)) {
            fox.setRollingHead(true);
            fox.setCrouching(true);
            fox.getNavigation().stop();
            fox.getLookControl().lookAt(livingEntity, (float)fox.getMaxHeadRotation(), (float)fox.getMaxLookPitchChange());
        } else {
            fox.setRollingHead(false);
            fox.setCrouching(false);
        }

    }

    public void tick() {
        LivingEntity livingEntity = fox.getTarget();
        if (livingEntity != null) {
            fox.getLookControl().lookAt(livingEntity, (float)fox.getMaxHeadRotation(), (float)fox.getMaxLookPitchChange());
            if (fox.squaredDistanceTo(livingEntity) <= 36.0) {
                fox.setRollingHead(true);
                fox.setCrouching(true);
                fox.getNavigation().stop();
            } else {
                fox.getNavigation().startMovingTo(livingEntity, 1.5);
            }

        }
    }
}
