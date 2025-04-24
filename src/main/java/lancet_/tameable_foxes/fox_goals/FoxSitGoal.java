package lancet_.tameable_foxes.fox_goals;


import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumSet;
import java.util.UUID;

import static net.minecraft.entity.passive.FoxEntity.OWNER;

public class FoxSitGoal extends Goal {
    private final FoxEntity fop;

    public FoxSitGoal(FoxEntity entity) {
        this.fop = entity;

        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    @Override
    public boolean shouldContinue() {return this.fop.isSitting() && !this.fop.isLeashed();}

    @Override
    public boolean canStart() {
        if (!isTamed()) {
            return false;
        } else if (this.fop.isInsideWaterOrBubbleColumn()) {
            return false;
        } else if (!this.fop.isOnGround()) {
            return false;
        } else {
            LivingEntity livingEntity = getOwner();
            if (livingEntity == null) {
                return true;
            } else {
                return (!(this.fop.squaredDistanceTo(livingEntity) < 144.0) || livingEntity.getAttacker() == null) && this.fop.isSitting();
            }
        }
    }

    @Override
    public void start() {
        this.fop.setMovementSpeed(0f);
        this.fop.getNavigation().stop();
        this.fop.setSitting(true);
        this.fop.setWalking(false);
    }

    @Override
    public void stop(){
        this.fop.setSitting(false);
    }

    @Unique
    public boolean isTamed(){
        return getOwnerUuid() != null;
    }

    public UUID getOwnerUuid(){
        assert this.fop != null;
        return this.fop.getDataTracker().get(OWNER).orElse(null);
    }

    @Nullable LivingEntity getOwner() {
        UUID uUID = this.getOwnerUuid();
        if (uUID == null) {
            return null;
        } else {
            return fop.getEntityWorld().getPlayerByUuid(uUID);
        }
    }
}
