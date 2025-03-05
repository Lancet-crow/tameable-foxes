package lancet_.tameable_foxes.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0;

public class FoxAttackWithOwnerGoal extends TargetGoal {
    private final Fox fop;
    private LivingEntity attacking;
    private int lastAttackTime;

    public FoxAttackWithOwnerGoal(Fox tameable) {
        super(tameable, false);
        this.fop = tameable;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.fop.isSitting()) {
            return false;
        }
        UUID uuid = this.fop.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        if(uuid == null) {
            return false;
        }
        LivingEntity livingEntity = Objects.requireNonNull(this.fop.getServer()).getPlayerList().getPlayer(uuid);
        if (livingEntity == null) {
            return false;
        }
        this.attacking = livingEntity.getLastAttacker();
        int i = livingEntity.getLastHurtByMobTimestamp();
        return i != this.lastAttackTime && this.canAttack(this.attacking, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        UUID uuid = this.fop.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        if(uuid != null) {
            LivingEntity livingEntity = Objects.requireNonNull(this.fop.getServer()).getPlayerList().getPlayer(uuid);
            if (livingEntity != null) {
                this.lastAttackTime = livingEntity.getLastHurtByMobTimestamp();
            }
        }
        super.start();
    }
}
