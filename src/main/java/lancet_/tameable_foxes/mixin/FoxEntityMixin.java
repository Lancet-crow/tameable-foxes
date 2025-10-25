package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import lancet_.tameable_foxes.goals.FoxBegGoal;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.entity.passive.TameableEntity.OWNER_UUID;


@Mixin(value = FoxEntity.class, priority = 1001)
public abstract class FoxEntityMixin extends AnimalEntity implements Tameable, TameableTricksInterface, Angerable {
    @Unique
    private static final TrackedData<Boolean> BEGGING = DataTracker.registerData(FoxEntityMixin.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(FoxEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    @Shadow
    @Final
    public static TrackedData<Optional<UUID>> OWNER;
    @Unique
    private final TameableEntity tame;
    @Unique
    @Nullable
    private UUID angryAt;

    protected FoxEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.tame = (TameableEntity) (Object) this;
    }

    @Shadow
    public abstract boolean isSitting();

    @Shadow
    public abstract void setSitting(boolean sitting);

    @Shadow
    public abstract void setSleeping(boolean sleeping);

    @Shadow
    public abstract void setWalking(boolean walking);

    @Shadow
    public abstract void playAmbientSound();

    @Shadow
    public abstract void addTrustedUuid(@Nullable UUID uuid);

    @Shadow
    abstract void setAggressive(boolean aggressive);

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FoxEntity;setSleeping(Z)V"))
    private void checkIfPreviousOwnerExists(NbtCompound nbt, CallbackInfo ci) {
        if (this.getTame().getOwner() == null && this.getDataTracker().get(OWNER).isPresent()) {
            this.getTame().setTamed(true, true);
            this.getTame().setOwnerUuid(this.getDataTracker().get(OWNER).orElse(null));
        }
        if (this.getTame().getOwner() != null && this.getDataTracker().get(OWNER).isEmpty()) {
            this.addTrustedUuid(this.getOwnerUuid());
        }
    }

    @Unique
    public TameableEntity getTame() {
        return this.tame == null ? (TameableEntity) (Object) this : this.tame;
    }

    @Override
    public TrackedData<Optional<UUID>> getOwnerTrackedData() {
        return OWNER;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();

        if (this.getWorld().isClient) {
            boolean bl = getTame().isOwner(player) || getTame().isTamed() ||
                    (isTamingItem(item) && !getTame().isTamed()) || isBreedingItem(itemStack);
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        } else if (getTame().isTamed()) {
            if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                float f = foodComponent != null ? foodComponent.nutrition() : 1.0F;
                this.heal(2.0F * f);
                return ActionResult.SUCCESS;
            } else {
                ActionResult actionResult = super.interactMob(player, hand);
                if ((!actionResult.isAccepted() || this.isBaby()) && getTame().isOwner(player)) {
                    if (this.isSleeping()) {
                        this.setSleeping(false);
                    } else {
                        this.setSitting(!this.isSitting());
                    }
                    this.getNavigation().stop();
                    return ActionResult.SUCCESS;
                } else {
                    return actionResult;
                }
            }
        } else if (isTamingItem(item) && TameableFoxesConfig.config.foxesTameDirectly) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            if (this.random.nextFloat() >= 1 - TameableFoxesConfig.config.foxesTamingChance) {
                getTame().setOwner(player);
                this.getDataTracker().set(OWNER, Optional.ofNullable(player.getUuid()));
                this.getNavigation().stop();
                this.setTarget(null);
                this.setSitting(true);
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }

            return ActionResult.SUCCESS;
        } else {
            return super.interactMob(player, hand);
        }
    }

    @WrapMethod(method = "setSitting")
    public void tameableFoxes$setSitting(boolean sitting, Operation<Void> original) {
        if (sitting) {
            this.setForwardSpeed(0f);
            this.setWalking(false);
        }
        this.getTame().setSitting(sitting);
        this.getTame().setInSittingPose(sitting);
        original.call(sitting);
    }

    @WrapMethod(method = "isSitting")
    public boolean tameableFoxes$isSitting(Operation<Boolean> original) {
        return this.getTame().isInSittingPose();
    }

    @Override
    protected boolean shouldFollowLeash() {
        FoxEntity foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;
        return !foxEntity.isSitting();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.getWorld().isClient) {
                this.setSitting(false);
            }
            return super.damage(source, amount);
        }
    }

    @WrapMethod(method = "onPlayerSpawnedChild")
    private void ownerWithSpawnEggs(PlayerEntity player, MobEntity child, Operation<Void> original) {
        if (child instanceof FoxEntity fox) {
            if (getTame().isTamed() || TameableFoxesConfig.config.foxesTameDirectly) {
                fox.getDataTracker().set(FoxEntity.OWNER, Optional.of(player.getUuid()));
            }
        }
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectTrackers(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(BEGGING, false);
        builder.add(ANGER_TIME, 0);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readAngerTime(NbtCompound nbt, CallbackInfo ci) {
        this.readAngerFromNbt(this.getWorld(), nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeAngerTime(NbtCompound nbt, CallbackInfo ci) {
        this.writeAngerToNbt(nbt);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void tickAnger(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld) this.getWorld(), true);
        }
    }

    @Unique
    private boolean isTamingItem(Item item) {
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(item);
    }

    @Inject(method = "isBreedingItem", at = @At("RETURN"), cancellable = true)
    public void tameable_foxes$isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem()));
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        } else if (!(other instanceof FoxEntity otherFox)) {
            return false;
        } else if (((TameableEntity) (Object) otherFox).isTamed() != getTame().isTamed()) {
            return false;
        } else {
            return !otherFox.isSitting() && !this.isSitting() && this.isInLove() && otherFox.isInLove();
        }
    }

    @ModifyReturnValue(method = "canPickupItem", at = @At("TAIL"))
    private boolean restrictPickItemIfShould(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !TameableFoxesConfig.ITEMS_RESTRICTED_TO_PICK.contains(stack.getItem());
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    public void addAiGoals(CallbackInfo ci) {
        FoxEntity foxEntity = (FoxEntity) (Object) this;

        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> (entity instanceof ChickenEntity || entity instanceof RabbitEntity) && !getTame().isTamed());
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test(entity) && !getTame().isTamed());
        foxEntity.followFishGoal = new ActiveTargetGoal<>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && !getTame().isTamed());
        foxEntity.goalSelector.add(0, new SitGoal(getTame()));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks()), false));
        foxEntity.targetSelector.add(1, new TrackOwnerAttackerGoal(getTame()));
        foxEntity.targetSelector.add(2, new AttackWithOwnerGoal(getTame()));

        foxEntity.goalSelector.getGoals().removeIf((goal) -> goal.getGoal() instanceof FleeEntityGoal<?>);
        foxEntity.goalSelector.getGoals().removeIf((goal) -> goal.getGoal() instanceof FoxEntity.DefendFriendGoal);
        foxEntity.goalSelector.getGoals().removeIf((goal) -> goal.getGoal() instanceof FoxEntity.AttackGoal);
        foxEntity.targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(this, PolarBearEntity.class, 8.0F, 1.6, 1.4, entity -> !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive() && TameableFoxesConfig.config.untamedWolvesAttackTamedFoxes));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !getTame().isTamed()
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(5, foxEntity.new AttackGoal(1.2f, true));

        foxEntity.goalSelector.add(6, new FollowOwnerGoal(getTame(), 1.0, 10.0f, 2.0f));

        foxEntity.goalSelector.add(7, new FoxBegGoal(foxEntity, 8.0F));
    }

    @Override
    public GoalSelector getFoxGoalSelector() {
        return this.goalSelector;
    }

    @Override
    public boolean isBegging() {
        return this.dataTracker.get(BEGGING);
    }

    @Override
    public void setBegging(boolean begging) {
        this.dataTracker.set(BEGGING, begging);
    }

    @Nullable
    @Override
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER).orElse(this.dataTracker.get(OWNER_UUID).orElse(null));
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof CreeperEntity || target instanceof GhastEntity) {
            return false;
        } else if (target instanceof WolfEntity wolfEntity) {
            return !wolfEntity.isTamed() || wolfEntity.getOwner() != owner;
        } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity) owner).shouldDamagePlayer((PlayerEntity) target)) {
            return false;
        } else {
            return target instanceof AbstractHorseEntity && ((AbstractHorseEntity) target).isTame()
                    ? false
                    : !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
        }
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
        if (angryAt != null) {
            this.setAggressive(true);
        }
    }

    @Override
    public void stopAnger() {
        this.setAttacker((LivingEntity) null);
        this.setAngryAt((UUID) null);
        this.setTarget((LivingEntity) null);
        this.setAngerTime(0);
        this.setAggressive(false);
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME, angerTime);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    public void addAmbientAggroWhenHasAngerTime(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.hasAngerTime()) {
            cir.setReturnValue(SoundEvents.ENTITY_FOX_AGGRO);
        }
    }
}