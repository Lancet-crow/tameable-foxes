package lancet_.tameable_foxes.fox_goals;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TamedFox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.entity.passive.FoxEntity.OWNER;

public class FoxAttackWithOwnerGoal extends TrackTargetGoal {
    private final FoxEntity fop;
    private LivingEntity attacking;
    private int lastAttackTime;

    public FoxAttackWithOwnerGoal(FoxEntity tameable) {
        super(tameable, false);
        this.fop = tameable;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (this.fop.isSitting()) {
            return false;
        }
        UUID uuid = ((TamedFox)this.fop).getOwnerUuid(this.fop);
        if(uuid == null) {
            return false;
        }
        LivingEntity livingEntity = Objects.requireNonNull(this.fop.getWorld().getServer()).getPlayerManager().getPlayer(uuid);
        if (livingEntity == null) {
            return false;
        }
        this.attacking = livingEntity.getAttacking();
        int i = livingEntity.getLastAttackTime();
        return i != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT) && canAttackWithOwner(this.attacking, livingEntity);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        UUID uuid = this.fop.getDataTracker().get(OWNER).orElse(null);
        if(uuid != null) {
            LivingEntity livingEntity = Objects.requireNonNull(this.fop.getWorld().getServer()).getPlayerManager().getPlayer(uuid);
            if (livingEntity != null) {
                this.lastAttackTime = livingEntity.getLastAttackTime();
            }
        }
        super.start();
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (!TameableFoxesConfig.config.foxesAttackWithOwner){
            return false;
        }
        else if (target instanceof CreeperEntity || target instanceof GhastEntity) {
            return false;
        } else if (target instanceof WolfEntity wolfEntity) {
            return !wolfEntity.isTamed() || wolfEntity.getOwner() != owner;
        }else if (target instanceof FoxEntity foxEntity) {
            UUID thisFoxUuid = ((TamedFox)this.fop).getOwnerUuid(this.fop);
            UUID otherFoxUuid = ((TamedFox)foxEntity).getOwnerUuid(foxEntity);
            return !Objects.equals(otherFoxUuid, thisFoxUuid);
        } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).shouldDamagePlayer((PlayerEntity)target)) {
            return false;
        } else {
            return (!(target instanceof AbstractHorseEntity) || !((AbstractHorseEntity) target).isTame()) && (!(target instanceof TameableEntity) || !((TameableEntity) target).isTamed());
        }
    }
}
