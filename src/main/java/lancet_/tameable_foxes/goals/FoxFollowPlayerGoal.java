package lancet_.tameable_foxes.goals;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import static net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0;

public class FoxFollowPlayerGoal extends Goal {
    private final Fox fop;
    private LivingEntity owner;
    private final double speed;
    private final float minDistance;
    private final float maxDistance;
    private int updateCountdownTicks;
    private final PathNavigation navigation;
    private float oldWaterPathfindingPenalty;
    public FoxFollowPlayerGoal(Fox entity, double speed, float minDistance, float maxDistance) {
        this.fop = entity;
        this.navigation = fop.getNavigation();
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    @Override
    public boolean canContinueToUse() {
        if (this.fop.isSitting() || this.fop.isSleeping()) {
            return false;
        }
        return !(this.fop.distanceToSqr(this.owner) <= (double)(this.maxDistance * this.maxDistance));
    }

    @Override
    public boolean canUse() {
        UUID uuid = this.fop.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        if(uuid == null) {
            return false;
        }
        LivingEntity livingEntity = Objects.requireNonNull(this.fop.getServer()).getPlayerList().getPlayer(uuid);
        if (livingEntity == null) {
            return false;
        }
        if (this.fop.isSitting()){
            return false;
        }
        if (livingEntity.isSpectator()) {
            return false;
        }
        if (this.fop.distanceToSqr(livingEntity) < (double)(this.minDistance * this.minDistance)) {
            return false;
        }
        owner = livingEntity;
        return true;
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.fop.getPathfindingMalus(PathType.WATER);
        this.fop.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.fop.setPathfindingMalus(PathType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        this.fop.getLookControl().setLookAt(this.owner, 10.0f, this.fop.getMaxHeadXRot());
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.adjustedTickDelay(10);
        if (this.fop.distanceToSqr(this.owner) >= 144.0) {
            this.tryTeleport();
        } else {
            this.navigation.moveTo(this.owner, this.speed);
        }
    }

    private void tryTeleport() {
        BlockPos blockPos = this.owner.getOnPos();
        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);
            boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (!bl) continue;
            return;
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0 && Math.abs((double)z - this.owner.getZ()) < 2.0) {
            return false;
        }
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.fop.lerpPositionAndRotationStep(1, x + 0.5, y, (double)z + 0.5, this.fop.getXRot(), this.fop.getYRot());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathType pathNodeType = WalkNodeEvaluator.getPathTypeStatic(fop, pos);
        if (pathNodeType != PathType.WALKABLE) {
            return false;
        }
        BlockState blockState = this.fop.getCommandSenderWorld().getBlockState(pos.below());
        BlockPos blockPos = pos.subtract(this.fop.blockPosition());
        return this.fop.getCommandSenderWorld().noCollision(this.fop, this.fop.getBoundingBox().move(blockPos));
    }

    private int getRandomInt(int min, int max) {
        return this.fop.getRandom().nextInt(max - min + 1) + min;
    }
}
