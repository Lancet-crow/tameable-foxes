package lancet_.tameable_foxes.goals;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FoxBegGoal extends Goal {
    private final Fox fox;
    private final Level world;
    private final float begDistance;
    private final TargetingConditions validPlayerPredicate;
    @Nullable
    private Player begFrom;
    private int timer;

    public FoxBegGoal(Fox fox, float begDistance) {
        this.fox = fox;
        this.world = fox.level();
        this.begDistance = begDistance;
        this.validPlayerPredicate = TargetingConditions.forNonCombat().range(begDistance);
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.begFrom = this.world.getNearestPlayer(this.validPlayerPredicate, this.fox);
        return this.begFrom != null && this.isAttractive(this.begFrom);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.begFrom != null && !this.begFrom.isAlive()) {
            return false;
        } else {
            return !(this.fox.distanceToSqr(this.begFrom) > this.begDistance * this.begDistance) && this.timer > 0 && this.isAttractive(this.begFrom);
        }
    }

    @Override
    public void start() {
        ((TameableTricksInterface) this.fox).setBegging(true);
        this.timer = this.adjustedTickDelay(40 + this.fox.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        ((TameableTricksInterface) this.fox).setBegging(false);
        this.begFrom = null;
    }

    @Override
    public void tick() {
        if (this.begFrom != null) {
            this.fox.getLookControl().setLookAt(this.begFrom.getX(), this.begFrom.getEyeY(), this.begFrom.getZ(), 10.0F, this.fox.getMaxHeadXRot());
        }
        this.timer--;
    }

    private boolean isAttractive(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (TameableFoxesConfig.getFoxTemptingItemStacks().anyMatch(attractiveStack -> attractiveStack.getItem().equals(itemStack.getItem()))) {
                return true;
            }
        }

        return false;
    }
}
