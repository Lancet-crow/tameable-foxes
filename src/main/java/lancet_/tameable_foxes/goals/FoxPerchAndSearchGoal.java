package lancet_.tameable_foxes.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;

public class FoxPerchAndSearchGoal extends Goal {
    private double relX;
    private double relZ;
    private int lookTime;
    private int looksRemaining;

    Fox fox;

    public FoxPerchAndSearchGoal(Fox fox) {
        this.fox = fox;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        alertableTargeting = TargetingConditions.forCombat()
                .range(12.0)
                .ignoreLineOfSight()
                .selector(this.fox.new FoxAlertableEntitiesSelector());
    }

    @Override
    public boolean canUse() {
        return fox.getLastHurtByMob() == null
                && fox.getRandom().nextFloat() < 0.02F
                && !fox.isSleeping()
                && fox.getTarget() == null
                && fox.getNavigation().isDone()
                && !this.alertable()
                && !fox.isPouncing()
                && !fox.isCrouching();
    }

    @Override
    public boolean canContinueToUse() {
        return this.looksRemaining > 0;
    }

    @Override
    public void start() {
        this.resetLook();
        this.looksRemaining = 2 + fox.getRandom().nextInt(3);
        fox.setSitting(true);
        fox.getNavigation().stop();
    }

    @Override
    public void stop() {
        fox.setSitting(false);
    }

    @Override
    public void tick() {
        this.lookTime--;
        if (this.lookTime <= 0) {
            this.looksRemaining--;
            this.resetLook();
        }

        fox.getLookControl()
                .setLookAt(
                        fox.getX() + this.relX,
                        fox.getEyeY(),
                        fox.getZ() + this.relZ,
                        (float)fox.getMaxHeadYRot(),
                        (float)fox.getMaxHeadXRot()
                );
    }

    private void resetLook() {
        double d0 = (Math.PI * 2) * fox.getRandom().nextDouble();
        this.relX = Math.cos(d0);
        this.relZ = Math.sin(d0);
        this.lookTime = this.adjustedTickDelay(80 + fox.getRandom().nextInt(20));
    }

    private final TargetingConditions alertableTargeting;

    protected boolean alertable() {
        return !fox.level()
                .getNearbyEntities(LivingEntity.class, this.alertableTargeting, fox, fox.getBoundingBox().inflate(12.0, 6.0, 12.0))
                .isEmpty();
    }
}
