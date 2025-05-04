package lancet_.tameable_foxes.fox_goals;

import lancet_.tameable_foxes.TamedFox;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import snownee.companion.CompanionCommonConfig;
import snownee.companion.Hooks;

import java.util.EnumSet;

public class FoxFollowPlayerGoal extends Goal {
    protected final FoxEntity fop;
    protected LivingEntity owner;
    private final double speed;
    private final float minDistance;
    private final float maxDistance;
    private int updateCountdownTicks;
    private final EntityNavigation navigation;
    private float oldWaterPathfindingPenalty;

    protected final boolean leavesAllowed;

    protected final WorldView world;

    public FoxFollowPlayerGoal(FoxEntity entity, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
        this.fop = entity;
        this.navigation = fop.getNavigation();
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        this.world = entity.getWorld();
        this.leavesAllowed = leavesAllowed;
    }
    @Override
    public boolean canStart() {
        PlayerEntity owner = ((TamedFox)this.fop).getOwner(this.fop);
        if(owner == null) {
            return false;
        }
        if (this.fop.isSitting() || (this.fop.isSleeping() && owner.isSneaking())){
            return false;
        }
        if (this.fop.goalSelector.getRunningGoals().anyMatch((goal) -> goal.getGoal() instanceof
        FoxEntity.AvoidDaylightGoal)){
            return false;
        }
        if (owner.isSpectator()) {
            return false;
        }
        if (this.fop.squaredDistanceTo(owner) < (double)(this.minDistance * this.minDistance)) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
        } else {
            return !this.cannotFollow() && !(this.fop.squaredDistanceTo(this.owner) <= (double) (this.maxDistance * this.maxDistance));
        }
    }

    private boolean cannotFollow() {
        return this.fop.isSitting() || this.fop.hasVehicle() || this.fop.isLeashed() || this.fop.isSleeping();
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.fop.getPathfindingPenalty(PathNodeType.WATER);
        this.fop.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.fop.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        this.fop.getLookControl().lookAt(this.owner, 10.0f, this.fop.getMaxLookPitchChange());
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            if (this.fop.squaredDistanceTo(this.owner) >= 144.0) {
                tryTeleport();
            } else {
                this.navigation.startMovingTo(this.owner, this.speed);
            }
        }
    }

    protected void tryTeleport() {
        BlockPos blockPos = this.owner.getBlockPos();
        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomInt(-3, 3);
            int k = this.getRandomInt(-1, 1);
            int l = this.getRandomInt(-3, 3);
            boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (bl) {
                return;
            }
        }
        if (FabricLoader.getInstance().isModLoaded("companion")){
            if (CompanionCommonConfig.petForceTeleportingIfFollowFailed && this.fop != null) {
                Hooks.teleportWithRandomOffset(this.fop, this.owner.getWorld(), this.owner.getBlockPos(), this.leavesAllowed, this.owner).ifPresent((vec) -> this.fop.requestTeleport(vec.x, vec.y, vec.z));
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0 && Math.abs((double)z - this.owner.getZ()) < 2.0) {
            return false;
        }
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        this.fop.refreshPositionAndAngles((double)x + 0.5, y, (double)z + 0.5, this.fop.getYaw(), this.fop.getPitch());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy());
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        }
        BlockState blockState = this.world.getBlockState(pos.down());
        if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
            return false;
        } else {
            BlockPos blockPos = pos.subtract(this.fop.getBlockPos());
            return this.world.isSpaceEmpty(this.fop, this.fop.getBoundingBox().offset(blockPos));
        }
    }

    private int getRandomInt(int min, int max) {
        return this.fop.getRandom().nextInt(max - min + 1) + min;
    }
}
