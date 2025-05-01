package lancet_.tameable_foxes.fox_goals;


import lancet_.tameable_foxes.TamedFox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;

import java.util.EnumSet;

public class FoxSitGoal extends Goal {
    private final FoxEntity fop;

    public FoxSitGoal(FoxEntity entity) {
        this.fop = entity;

        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    @Override
    public boolean shouldContinue() {return this.fop.isSitting(); }

    @Override
    public boolean canStart() {
        if (!((TamedFox)fop).isTamed(fop)) {
            return false;
        } else if (this.fop.isInsideWaterOrBubbleColumn()) {
            return false;
        } else if (!this.fop.isOnGround()) {
            return false;
        } else {
            LivingEntity livingEntity = ((TamedFox)fop).getOwner(fop);
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
}
