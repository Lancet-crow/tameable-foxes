package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.fox_goals.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.EntityView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.entity.passive.FoxEntity.OWNER;


@Mixin(FoxEntity.class)
public abstract class FoxMixin extends AnimalEntity implements Tameable {
    @Shadow public abstract boolean isSitting();

    @Unique
    FoxEntity foxEntity;
    protected FoxMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        foxEntity = (FoxEntity) (Object) entityType;
    }
    @Unique
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        UUID uuid = foxEntity.getDataTracker().get(OWNER).orElse(null);
        ActionResult actionResult = super.interactMob(player,hand);
        if (uuid == null && isTamingItem(player, hand) && (TameableFoxesConfig.config.foxesTameDirectly || !isBreedingItem(player.getStackInHand(hand)))){
            this.eat(player, hand, player.getStackInHand(hand));
            this.lovePlayer(player);
            setFoxOwner(player.getUuid());
            if (this.getWorld().isClient) {
                return ActionResult.CONSUME;
            }
            else{
                return ActionResult.SUCCESS;
            }
        }
        else if (isBreedingItem(player.getStackInHand(hand))){
            return actionResult;
        }
        if (!player.getUuid().equals(uuid)) return ActionResult.PASS;
        if (foxEntity.isSleeping()){
            foxEntity.setSleeping(false);
        }
        else{
            foxEntity.setSitting(!foxEntity.isSitting());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean shouldFollowLeash() {
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;
        return !foxEntity.isSitting();
    }

    @Unique
    public boolean isTamed(){
        return getOwnerUuid() != null;
    }

    public UUID getOwnerUuid(){
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;
        return foxEntity.getDataTracker().get(OWNER).orElse(null);
    }

    @Unique
    private boolean isTamingItem(PlayerEntity player, Hand hand){
        ItemStack itemStack = player.getStackInHand(hand);
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(itemStack.getItem());
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other instanceof FoxEntity fox && other != foxEntity){
            boolean isOtherFoxTamed = fox.getDataTracker().get(OWNER).orElse(null) != null;
            if (isTamed() == isOtherFoxTamed){
                return !fox.isSitting() && this.isInLove() && fox.isInLove();
            }
        }
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack){
        return TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem());
    }

    @Override
    public boolean beforeLeashTick(Entity leashHolder, float distance) {
        if (this.isSitting()) {
            if (distance > 10.0F) {
                this.detachLeash(true, true);
            }

            return false;
        } else {
            return super.beforeLeashTick(leashHolder, distance);
        }
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    public void addAiGoals(CallbackInfo ci) {
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;

        foxEntity.goalSelector.getGoals().removeIf(goal ->
                goal.getGoal() instanceof FoxEntity.SitDownAndLookAroundGoal ||
                        goal.getGoal() instanceof FoxEntity.AvoidDaylightGoal ||
                        goal.getGoal() instanceof FoxEntity.MoveToHuntGoal);

        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> (entity instanceof ChickenEntity || entity instanceof RabbitEntity) && !isTamed());
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test(entity) && !isTamed());
        foxEntity.followFishGoal = new ActiveTargetGoal<>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && !isTamed());
        foxEntity.goalSelector.add(1, new FoxSitGoal(foxEntity));
        foxEntity.goalSelector.add(1, new FoxAttackWithOwnerGoal(foxEntity));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks()), false));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !isTamed()
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(5, new FoxMoveToHuntGoal(foxEntity));
        foxEntity.goalSelector.add(6, new FoxFollowPlayerGoal(foxEntity, 1.0, 10.0f, 2.0f, false));
        foxEntity.goalSelector.add(6, new FoxAvoidDaylightGoal(foxEntity, 1.25));
        foxEntity.goalSelector.add(13, new FoxSitDownAndLookAroundGoal(foxEntity));
    }
    @Unique
    public void setFoxOwner(UUID newOwnerUUID){
        assert foxEntity != null;
        foxEntity.getDataTracker().set(OWNER, Optional.of(newOwnerUUID));
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld() instanceof ServerWorld serverWorld &&
                serverWorld.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)
                && this.getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.sendMessage(this.getDamageTracker().getDeathMessage());
        }

        super.onDeath(damageSource);
    }
}