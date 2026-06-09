package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import lancet_.tameable_foxes.goals.FoxBegGoal;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = Fox.class, priority = 1002)
public abstract class FoxEntityMixin extends Animal implements OwnableEntity, TameableTricksInterface, NeutralMob {
    @Unique
    private static final EntityDataAccessor<Boolean> BEGGING = SynchedEntityData.defineId(FoxEntityMixin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(FoxEntityMixin.class, EntityDataSerializers.INT);
    @Unique
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);

    @Shadow
    @Final
    public static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0;
    @Unique
    private final TamableAnimal tame;

    @Unique
    @Nullable
    private UUID angryAt;

    protected FoxEntityMixin(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
        this.tame = (TamableAnimal) (Object) this;
    }

    @Shadow
    protected abstract void usePlayerItem(Player player, InteractionHand hand, ItemStack stack);

    @Shadow
    public abstract boolean isSitting();

    @Shadow
    public abstract void setSitting(boolean sitting);

    @Shadow
    public abstract boolean isFood(ItemStack stack);

    @Shadow
    public abstract boolean isSleeping();

    @Shadow
    public abstract void setSleeping(boolean sleeping);

    @Shadow
    abstract void setFaceplanted(boolean walking);

    @Shadow
    public abstract void playAmbientSound();

    @Shadow abstract void addTrustedUUID(@Nullable UUID uUID);

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;setSleeping(Z)V"))
    private void checkIfPreviousOwnerExists(CompoundTag compoundTag, CallbackInfo ci) {
        if (this.getTame().getOwner() == null && this.getEntityData().get(DATA_TRUSTED_ID_0).isPresent()) {
            this.getTame().setTame(true);
            this.getTame().setOwnerUUID(this.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null));
        }
        if (this.getTame().getOwner() != null && this.getEntityData().get(DATA_TRUSTED_ID_0).isEmpty()) {
            this.addTrustedUUID(this.getOwnerUUID());
        }
    }

    @Unique
    public TamableAnimal getTame() {
        return this.tame == null ? (TamableAnimal) (Object) this : this.tame;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
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

                this.heal((float) (item.getFoodProperties() != null ? item.getFoodProperties().getNutrition() : 0));
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult actionResult = super.mobInteract(player, hand);
                if ((!actionResult.consumesAction() || this.isBaby()) && getTame().isOwnedBy(player)) {
                    if (this.isSleeping()) {
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
                this.addTrustedUUID(player.getUUID());
                this.getNavigation().stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
            } else {
                this.level().broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Inject(method = "setSitting", at = @At("TAIL"))
    public void tameableFoxes$setSitting(boolean bl, CallbackInfo ci) {
        if (bl) {
            this.setZza(0f);
            this.setFaceplanted(false);
        }
        this.getTame().setOrderedToSit(bl);
        this.getTame().setInSittingPose(bl);
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
                this.setSitting(false);
            }
            return super.hurt(source, amount);
        }
    }

    @WrapMethod(method = "onOffspringSpawnedFromEgg")
    private void ownerWithSpawnEggs(Player player, Mob mob, Operation<Void> original) {
        if (mob instanceof Fox fox) {
            if (getTame().isTame() || TameableFoxesConfig.config.foxesTrustOnBorn) {
                ((TameableTricksInterface)fox).getTame().tame(player);
                original.call(player, mob);
            }
        }
    }

    @ModifyExpressionValue(method = "trusts", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
    private boolean tameableFoxes$trusts(boolean original, @Local(argsOnly = true) UUID uUID){
        return original || Objects.equals(getTame().getOwnerUUID(), uUID);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void injectTrackers(CallbackInfo ci) {
        this.entityData.define(BEGGING, false);
        this.entityData.define(ANGER_TIME, 0);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAngerTime(CompoundTag compoundTag, CallbackInfo ci) {
        this.readPersistentAngerSaveData(this.level(), compoundTag);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeAngerTime(CompoundTag compoundTag, CallbackInfo ci) {
        this.addPersistentAngerSaveData(compoundTag);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void tickAnger(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
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

    @Override
    public boolean canMate(Animal other) {
        if (other == this) {
            return false;
        } else if (!(other instanceof Fox otherFox)) {
            return false;
        } else if (((TameableTricksInterface)otherFox).getTame().isTame() != getTame().isTame()) {
            return false;
        } else {
            return !otherFox.isSitting() && !this.isSitting() && this.isInLove() && otherFox.isInLove();
        }
    }

    @WrapOperation(method = "clearStates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;setSitting(Z)V"))
    private void restrictUnsittingIfOrderedToSit(Fox instance, boolean bl, Operation<Void> original){
        if (!this.getTame().isOrderedToSit()){
            original.call(instance, bl);
        }
    }

    @ModifyReturnValue(method = "canHoldItem", at = @At("TAIL"))
    private boolean restrictPickItemIfShould(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !TameableFoxesConfig.ITEMS_RESTRICTED_TO_PICK.contains(stack.getItem());
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void addAiGoals(CallbackInfo ci) {
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

        foxEntity.goalSelector.addGoal(6, new FollowOwnerGoal(getTame(), 1.0, 10.0f, 2.0f, false));

        foxEntity.goalSelector.addGoal(7, new FoxBegGoal(foxEntity, 8.0F));
    }

    @Override
    public GoalSelector tameable_foxes$getFoxGoalSelector() {
        return this.goalSelector;
    }

    @Override
    public boolean tameable_foxes$isBegging() {
        return this.getEntityData().get(BEGGING);
    }

    @Override
    public void tameable_foxes$setBegging(boolean begging) {
        this.getEntityData().set(BEGGING, begging);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.getEntityData().get(DATA_TRUSTED_ID_0).orElse(this.getEntityData().get(TamableAnimal.DATA_OWNERUUID_ID).orElse(null));
    }

    @Override
    public boolean canAttack(LivingEntity target){
        return getTame().isTame() ? TameableFoxesConfig.config.foxesAttackWithOwner : super.canAttack(target);
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
        if (Objects.equals(getTame().getOwnerUUID(), angryAt)){
            return;
        }
        this.angryAt = angryAt;
        if (angryAt != null) {
            this.setAggressive(true);
        }
    }

    @Override
    public void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
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

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean bl = target.hurt(this.damageSources().mobAttack(this), (int) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (bl) {
            this.doEnchantDamageEffects(this, target);
        }
        return bl;
    }
}