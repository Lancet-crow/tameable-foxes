package lancet_.tameable_foxes.fox_goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

import static net.minecraft.entity.passive.FoxEntity.OWNER;

public class FoxSitDownAndLookAroundGoal extends Goal {
    private double lookX;
    private double lookZ;
    private int timer;
    private int counter;

    private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

    FoxEntity fox;

    public FoxSitDownAndLookAroundGoal(FoxEntity foxEntity) {
        assert foxEntity != null;
        this.fox = foxEntity;
        WORRIABLE_ENTITY_PREDICATE = TargetPredicate.createAttackable()
                .setBaseMaxDistance(12.0)
                .ignoreVisibility()
                .setPredicate(fox.new WorriableEntityFilter());
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return fox.getAttacker() == null
                && fox.getRandom().nextFloat() < 0.02F
                && !fox.isSleeping()
                && fox.getTarget() == null
                && fox.getNavigation().isIdle()
                && !this.canCalmDown()
                && !fox.isChasing()
                && !fox.isInSneakingPose()
                && fox.getDataTracker().get(OWNER).orElse(null) == null;
    }

    @Override
    public boolean shouldContinue() {
        return this.counter > 0;
    }

    @Override
    public void start() {
        this.chooseNewAngle();
        this.counter = 2 + fox.getRandom().nextInt(3);
        fox.setSitting(true);
        fox.getNavigation().stop();
    }

    @Override
    public void stop() {
        fox.setSitting(false);
    }

    @Override
    public void tick() {
        this.timer--;
        if (this.timer <= 0) {
            this.counter--;
            this.chooseNewAngle();
        }

        fox.getLookControl()
                .lookAt(
                        fox.getX() + this.lookX,
                        fox.getEyeY(),
                        fox.getZ() + this.lookZ,
                        (float)fox.getMaxHeadRotation(),
                        (float)fox.getMaxLookPitchChange()
                );
    }

    private void chooseNewAngle() {
        double d = (Math.PI * 2) * fox.getRandom().nextDouble();
        this.lookX = Math.cos(d);
        this.lookZ = Math.sin(d);
        this.timer = this.getTickCount(80 + fox.getRandom().nextInt(20));
    }



    protected boolean isAtFavoredLocation() {
        BlockPos blockPos = BlockPos.ofFloored(fox.getX(), fox.getBoundingBox().maxY, fox.getZ());
        return !fox.getWorld().isSkyVisible(blockPos) && fox.getPathfindingFavor(blockPos) >= 0.0F;
    }

    protected boolean canCalmDown() {
        return !fox.getWorld()
                .getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, fox, fox.getBoundingBox().expand(12.0, 6.0, 12.0))
                .isEmpty();
    }
}
