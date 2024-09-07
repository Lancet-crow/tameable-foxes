package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.FoxAttackWithOwnerGoal;
import lancet_.tameable_foxes.FoxFollowPlayerGoal;
import lancet_.tameable_foxes.FoxSitGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static net.minecraft.entity.passive.FoxEntity.OWNER;


@Mixin(FoxEntity.class)
@SuppressWarnings("unused")
public abstract class FoxMixin extends AnimalEntity {

    protected FoxMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    @Unique
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        FoxEntity foxEntity = ((FoxEntity) (Object) this);
        ActionResult actionResult = super.interactMob(player,hand);
        if(actionResult.isAccepted()) return actionResult;
        UUID uuid = foxEntity.getDataTracker().get(OWNER).orElse(null);
        if (!player.getUuid().equals(uuid)) return ActionResult.PASS;
        foxEntity.setSitting(!foxEntity.isSitting());
        this.jumping = false;
        this.navigation.stop();
        this.setTarget(null);
        return ActionResult.SUCCESS;
    }
    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    public void addAiGoals(CallbackInfo ci) {
        FoxEntity foxEntity = ((FoxEntity) (Object) this);
        //followChickenAndRabbitGoal
        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<AnimalEntity>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> OWNER != null &&
                (entity instanceof ChickenEntity || entity instanceof RabbitEntity));
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<TurtleEntity>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test((LivingEntity)entity) && OWNER != null);
        foxEntity.followFishGoal = new ActiveTargetGoal<FishEntity>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && OWNER != null);
        foxEntity.goalSelector.add(0, foxEntity.new FoxSwimGoal());
        foxEntity.goalSelector.add(3, foxEntity.new MateGoal(1.0));
        foxEntity.goalSelector.add(6, foxEntity.new JumpChasingGoal());
        foxEntity.goalSelector.add(11, foxEntity.new PickupItemGoal());
        foxEntity.goalSelector.add(12, foxEntity.new LookAtEntityGoal(foxEntity, PlayerEntity.class, 24.0f));
        foxEntity.goalSelector.add(7, foxEntity.new AttackGoal((double) 1.2f, true));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<WolfEntity>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<PolarBearEntity>(foxEntity, PolarBearEntity.class,
                8.0f, 1.6, 1.4, entity -> !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(10, new PounceAtTargetGoal(foxEntity, 0.4f));
        foxEntity.goalSelector.add(11, new WanderAroundFarGoal(foxEntity, 1.0));
        foxEntity.targetSelector.add(3, (foxEntity.new DefendFriendGoal(LivingEntity.class, false,
                false, entity -> FoxEntity.JUST_ATTACKED_SOMETHING_FILTER.test((Entity) entity) &&
                !foxEntity.canTrust(entity.getUuid()))));
        foxEntity.goalSelector.add(1, new FoxSitGoal(foxEntity));
        foxEntity.goalSelector.add(1, new FoxAttackWithOwnerGoal(foxEntity));
        foxEntity.goalSelector.add(6, new FoxFollowPlayerGoal(foxEntity, 1.0, 10.0f, 2.0f));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<PlayerEntity>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && !EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !foxEntity.canTrust(e.getUuid())
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(new ItemStack(Items.SWEET_BERRIES)), false));
        ci.cancel();
    }
}