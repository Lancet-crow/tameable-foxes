package lancet_.tameable_foxes.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0;

public class FoxSitGoal extends Goal {
    private final Fox fop;
    private LivingEntity owner = null;

    public FoxSitGoal(Fox entity) {
        this.fop = entity;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.fop.isSitting() && !this.fop.isLeashed();
    }

    @Override
    public boolean canUse() {
        if (this.fop.isInWaterOrBubble()) {
            return false;
        }
        if (!this.fop.onGround()) {
            return false;
        }
        UUID uuid = this.fop.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        if(uuid == null) {
            return false;
        }
        LivingEntity livingEntity = Objects.requireNonNull(this.fop.getServer()).getPlayerList().getPlayer(uuid);
        if (livingEntity == null) {
            return true;
        }
        if (this.fop.distanceToSqr(livingEntity) < 144.0 && livingEntity.getLastAttacker() != null) {
            return false;
        }
        owner = livingEntity;
        return this.fop.isSitting();
    }

    @Override
    public void start() {
        this.fop.setSpeed(0f);
        this.fop.getNavigation().stop();
    }

}
