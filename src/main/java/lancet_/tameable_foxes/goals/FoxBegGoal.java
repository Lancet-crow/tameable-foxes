package lancet_.tameable_foxes.goals;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FoxBegGoal extends Goal {
    private final FoxEntity fox;
    @Nullable
    private PlayerEntity begFrom;
    private final World world;
    private final float begDistance;
    private int timer;
    private final TargetPredicate validPlayerPredicate;
    private final List<ItemStack> attractiveItems;

    public FoxBegGoal(FoxEntity fox, float begDistance) {
        this.fox = fox;
        this.world = fox.getWorld();
        this.begDistance = begDistance;
        this.validPlayerPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(begDistance);
        this.setControls(EnumSet.of(Goal.Control.LOOK));
        this.attractiveItems = TameableFoxesConfig.getFoxTemptingItemStacks().toList();
    }

    @Override
    public boolean canStart() {
        this.begFrom = this.world.getClosestPlayer(this.validPlayerPredicate, this.fox);
        return this.begFrom != null && this.isAttractive(this.begFrom);
    }

    @Override
    public boolean shouldContinue() {
        if (this.begFrom != null && !this.begFrom.isAlive()) {
            return false;
        } else {
            return !(this.fox.squaredDistanceTo(this.begFrom) > this.begDistance * this.begDistance) && this.timer > 0 && this.isAttractive(this.begFrom);
        }
    }

    @Override
    public void start() {
        ((TameableTricksInterface)this.fox).setBegging(true);
        this.timer = this.getTickCount(40 + this.fox.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        ((TameableTricksInterface)this.fox).setBegging(false);
        this.begFrom = null;
    }

    @Override
    public void tick() {
        if (this.begFrom != null){
            this.fox.getLookControl().lookAt(this.begFrom.getX(), this.begFrom.getEyeY(), this.begFrom.getZ(), 10.0F, this.fox.getMaxLookPitchChange());
        }
        this.timer--;
    }

    private boolean isAttractive(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (attractiveItems.stream().anyMatch(attractiveStack -> attractiveStack.getItem().equals(itemStack.getItem()))){
                return true;
            }
        }

        return false;
    }
}
