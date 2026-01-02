package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import lancet_.tameable_foxes.goals.FoxBegGoal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(Fox.class)
public abstract class FoxMixin extends Animal implements OwnableEntity, NeutralMob, TameableTricksInterface {

    @Shadow
    abstract void setSleeping(boolean sleeping);

    @Shadow public abstract void setSitting(boolean sitting);

    @Shadow public abstract boolean isSitting();

    @Shadow(aliases = "DATA_TRUSTED_ID_0") public static EntityDataAccessor<Optional<UUID>> OWNER;

    @Shadow
    abstract void setFaceplanted(boolean faceplanted);

    @Shadow
    abstract void addTrustedUUID(@Nullable UUID uuid);

    @Shadow public abstract Fox.Type getVariant();

    @Shadow protected abstract void spitOutItem(ItemStack stack);

    @Unique
    private static final EntityDataAccessor<Boolean> BEGGING = SynchedEntityData.defineId(FoxMixin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(FoxMixin.class, EntityDataSerializers.INT);
    @Unique
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);

    @Unique
    TamableAnimal tame;

    @Unique
    @Nullable
    private UUID angryAt;

    public FoxMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.tame = (TamableAnimal) (Object) this;
    }

    public TamableAnimal getTame() {
        return this.tame == null ? (TamableAnimal) (Object) this : this.tame;
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;setSleeping(Z)V"))
    private void checkIfPreviousOwnerExists(CompoundTag compound, CallbackInfo ci) {
        if (this.getTame().getOwner() == null && this.getEntityData().get(OWNER).isPresent()) {
            this.getTame().setTame(true, true);
            this.getTame().setOwnerUUID(this.getEntityData().get(OWNER).orElse(null));
        }
        if (this.getTame().getOwner() != null && this.getEntityData().get(OWNER).isEmpty()) {
            this.addTrustedUUID(this.getOwnerUUID());
        }
    }

    @Override
    public EntityDataAccessor<Optional<UUID>> getOwnerTrackedData() {
        return OWNER;
    }

    @Override
    public boolean canMate(@NotNull Animal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        } else if (!(otherAnimal instanceof Fox otherFox)) {
            return false;
        } else if (((TamableAnimal) (Object) otherFox).isTame() != getTame().isTame()) {
            return false;
        } else {
            return !otherFox.isSitting() && !this.isSitting() && this.isInLove() && otherFox.isInLove();
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        Fox fox = EntityType.FOX.create(level);
        if (fox != null) {
            fox.setVariant(this.random.nextBoolean() ? this.getVariant() : ((Fox)otherParent).getVariant());
            if (this.getOwner() != null){
                fox.getEntityData().set(OWNER, Optional.ofNullable(this.getOwnerUUID()));
            }
        }
        return fox;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.level().isClientSide) {
            boolean bl = getTame().isOwnedBy(player) || getTame().isTame() ||
                    (isTamingItem(item) && !getTame().isTame()) || isFood(itemStack);
            return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (getTame().isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                FoodProperties foodComponent = itemStack.get(DataComponents.FOOD);
                float f = foodComponent != null ? foodComponent.nutrition() : 1.0F;
                this.heal(2.0F * f);
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult actionResult = super.mobInteract(player, hand);
                if ((!actionResult.consumesAction() || this.isBaby()) && getTame().isOwnedBy(player)) {
                    if (this.isSleeping()) {
                        if (player.isDiscrete()){
                            ItemStack stackInMouth = this.getItemBySlot(EquipmentSlot.MAINHAND);
                            if (!stackInMouth.isEmpty()){
                                this.spitOutItem(stackInMouth);
                                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            }
                        }
                        this.setSleeping(false);
                    } else {
                        this.setSitting(!this.isSitting());
                    }
                    this.getNavigation().stop();
                    return InteractionResult.SUCCESS;
                } else {
                    return actionResult;
                }
            }
        } else if (isTamingItem(item) && TameableFoxesConfig.config.foxesTameDirectly) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            if (this.random.nextFloat() >= 1 - TameableFoxesConfig.config.foxesTamingChance) {
                getTame().tame(player);
                this.getEntityData().set(OWNER, Optional.ofNullable(player.getUUID()));
                this.getNavigation().stop();
                this.setTarget(null);
                this.setSitting(true);
                this.level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
            } else {
                this.level().broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Unique
    private boolean isTamingItem(Item item) {
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(item);
    }

    @Inject(method = "isFood", at = @At("RETURN"), cancellable = true)
    public void tameable_foxes$isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem()));
    }

    @Inject(method = "registerGoals", at = @At(value = "TAIL"))
    public void addAiGoals(CallbackInfo ci){
        Fox foxEntity = (Fox) (Object) this;

        foxEntity.landTargetGoal = new NearestAttackableTargetGoal<>((
                foxEntity), Animal.class, 10, false,
                false, entity -> (entity instanceof Chicken || entity instanceof Rabbit) && !getTame().isTame());
        foxEntity.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(foxEntity, Turtle.class,
                10, false, false,
                entity -> Turtle.BABY_ON_LAND_SELECTOR.test(entity) && !getTame().isTame());
        foxEntity.fishTargetGoal = new NearestAttackableTargetGoal<>(foxEntity, AbstractFish.class, 20,
                false, false, entity -> entity instanceof AbstractSchoolingFish && !getTame().isTame());
        foxEntity.goalSelector.addGoal(0, new SitWhenOrderedToGoal(getTame()));
        foxEntity.goalSelector.addGoal(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.of(TameableFoxesConfig.getFoxTemptingItemStacks()), false));
        foxEntity.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(getTame()));
        foxEntity.targetSelector.addGoal(2, new OwnerHurtTargetGoal(getTame()));

        foxEntity.goalSelector.getAvailableGoals().removeIf((goal) -> goal.getGoal() instanceof AvoidEntityGoal<?>);
        foxEntity.goalSelector.getAvailableGoals().removeIf((goal) -> goal.getGoal() instanceof Fox.DefendTrustedTargetGoal);
        foxEntity.goalSelector.getAvailableGoals().removeIf((goal) -> goal.getGoal() instanceof Fox.FoxMeleeAttackGoal);
        foxEntity.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        foxEntity.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 8.0F, 1.6, 1.4, entity -> !foxEntity.isAggressive()));
        foxEntity.goalSelector.addGoal(4, new AvoidEntityGoal<>(foxEntity, Wolf.class, 8.0f,
                1.6, 1.4, entity -> !((Wolf) entity).isTame() && !foxEntity.isAggressive() && TameableFoxesConfig.config.untamedWolvesAttackTamedFoxes));
        foxEntity.goalSelector.addGoal(4, new AvoidEntityGoal<>(foxEntity,
                Player.class, 16.0f, 1.6, 1.4, e -> !e.isDiscrete()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && !getTame().isTame()
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.addGoal(5, foxEntity.new FoxMeleeAttackGoal(1.2f, true));

        foxEntity.goalSelector.addGoal(6, new FollowOwnerGoal(getTame(), 1.0, 10.0f, 2.0f));

        foxEntity.goalSelector.addGoal(7, new FoxBegGoal(foxEntity, 8.0F));
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.getEntityData().get(OWNER).orElse(null);
    }

    @Unique
    public boolean isTame() {
        return this.getOwnerUUID() != null;
    }

    @WrapMethod(method = "setSitting")
    public void tameableFoxes$setSitting(boolean sitting, Operation<Void> original) {
        if (sitting) {
            this.setZza(0f);
            this.setFaceplanted(false);
        }
        this.getTame().setOrderedToSit(sitting);
        this.getTame().setInSittingPose(sitting);
        original.call(sitting);
    }

    @WrapMethod(method = "isSitting")
    public boolean tameableFoxes$isSitting(Operation<Boolean> original) {
        return this.getTame().isInSittingPose();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.getTame().setOrderedToSit(false);
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    public boolean isBegging() {
        return this.entityData.get(BEGGING);
    }

    @Override
    public void setBegging(boolean begging) {
        this.entityData.set(BEGGING, begging);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void injectTrackers(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BEGGING, false);
        builder.define(ANGER_TIME, 0);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAngerTime(CompoundTag compound, CallbackInfo ci) {
        this.readPersistentAngerSaveData(this.level(), compound);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeAngerTime(CompoundTag compound, CallbackInfo ci) {
        this.addPersistentAngerSaveData(compound);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void tickAnger(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
        }
    }

    @WrapMethod(method = "onOffspringSpawnedFromEgg")
    private void ownerWithSpawnEggs(Player player, Mob child, Operation<Void> original) {
        if (child instanceof Fox fox) {
            if (getTame().isTame() || TameableFoxesConfig.config.foxesTameDirectly) {
                fox.getEntityData().set(OWNER, Optional.of(player.getUUID()));
            }
        }
    }

    @ModifyReturnValue(method = "canHoldItem", at = @At("TAIL"))
    private boolean restrictPickItemIfShould(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !TameableFoxesConfig.ITEMS_RESTRICTED_TO_PICK.contains(stack.getItem());
    }

    @Override
    public GoalSelector getFoxGoalSelector() {
        return this.goalSelector;
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || target instanceof Ghast) {
            return false;
        } else if (target instanceof Wolf wolfEntity) {
            return !wolfEntity.isTame() || wolfEntity.getOwner() != owner;
        } else if (target instanceof Player && owner instanceof Player && !((Player) owner).canHarmPlayer((Player) target)) {
            return false;
        } else {
            return (!(target instanceof AbstractHorse) || !((AbstractHorse) target).isTamed())
                    && (!(target instanceof TamableAnimal) || !((TamableAnimal) target).isTame())
                    && TameableFoxesConfig.config.foxesAttackWithOwner;
        }
    }


    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.angryAt;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
        if (angryAt != null) {
            this.setAggressive(true);
        }
    }

    @Override
    public void stopBeingAngry() {
        this.setLastHurtByMob((LivingEntity) null);
        this.setPersistentAngerTarget((UUID) null);
        this.setTarget((LivingEntity) null);
        this.setRemainingPersistentAngerTime(0);
        this.setAggressive(false);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.getEntityData().get(ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int angerTime) {
        this.getEntityData().set(ANGER_TIME, angerTime);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(ANGER_TIME_RANGE.sample(this.random));
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    public void addAmbientAggroWhenHasAngerTime(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.isAngry()) {
            cir.setReturnValue(SoundEvents.FOX_AGGRO);
        }
    }
}
