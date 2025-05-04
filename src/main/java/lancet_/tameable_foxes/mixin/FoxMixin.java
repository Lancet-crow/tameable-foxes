package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TamedFox;
import lancet_.tameable_foxes.fox_goals.*;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;


@Mixin(FoxEntity.class)
public abstract class FoxMixin extends AnimalEntity implements Tameable, TamedFox {
    @Shadow public abstract boolean isSitting();

    @Shadow public abstract void playAmbientSound();

    @Shadow public abstract boolean isSleeping();

    @Shadow protected abstract void spit(ItemStack stack);

    @Shadow @Final public static TrackedData<Optional<UUID>> OWNER;

    @Shadow public abstract boolean canPickupItem(ItemStack stack);

    @Unique
    FoxEntity foxEntity;
    protected FoxMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        foxEntity = (FoxEntity) (Object) this;
    }
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();

        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;

        if (this.getWorld().isClient) {
            boolean bl = this.isOwner(foxEntity, player) || this.isTamed(foxEntity) ||
                    (isTamingItem(item) && !this.isTamed(foxEntity)) || isBreedingItem(itemStack);
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        } else if (this.isTamed(foxEntity)) {
            if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                this.heal((float) (item.getFoodComponent() != null ? item.getFoodComponent().getHunger() : 0));
                return ActionResult.SUCCESS;
            } else {
                ActionResult actionResult = super.interactMob(player, hand);
                if ((!actionResult.isAccepted() || this.isBaby()) && this.isOwner(foxEntity, player)) {
                    if (this.isSleeping()){
                        foxEntity.setSleeping(false);
                    }
                    else{
                        foxEntity.setSitting(!this.isSitting());
                    }
                    this.jumping = false;
                    this.getNavigation().stop();
                    this.setTarget(null);
                    return ActionResult.SUCCESS;
                } else {
                    if (this.isSleeping()){
                        ItemStack stackInMouth = this.getEquippedStack(EquipmentSlot.MAINHAND);
                        if (!stackInMouth.isEmpty()){
                            this.spit(stackInMouth);
                            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        }
                    }
                    return actionResult;
                }
            }
        } else if (isTamingItem(item) && TameableFoxesConfig.config.foxesTameDirectly) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            this.playSound(this.getEatSound(itemStack), 1.0F, 1.0F);
            if (this.random.nextFloat() >= 1 - TameableFoxesConfig.config.foxesTamingChance) {
                this.setFoxOwner(player);
                this.navigation.stop();
                this.setTarget(null);
                foxEntity.setSitting(true);
                this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                this.getEntityWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }

            return ActionResult.SUCCESS;
        } else {
            return super.interactMob(player, hand);
        }
    }

    @WrapMethod(method = "onPlayerSpawnedChild")
    private void ownerWithSpawnEggs(PlayerEntity player, MobEntity child, Operation<Void> original){
        if (child instanceof FoxEntity fox){
            if (this.isTamed(foxEntity) || TameableFoxesConfig.config.foxesTameDirectly){
                fox.getDataTracker().set(OWNER, Optional.of(player.getUuid()));
            }
        }
    }

    @Override
    protected boolean shouldFollowLeash() {
        return !isSitting();
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    public void handleReactionsStatus(byte status, CallbackInfo ci){
        if (status == EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(true);
        } else if (status == EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES) {
            this.showEmoteParticle(false);
        }
    }

    @Unique
    protected void showEmoteParticle(boolean positive) {
        ParticleEffect particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticle(particleEffect, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Unique
    private boolean isTamingItem(Item item){
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(item);
    }

    public UUID getOwnerUuid(){
        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        return this.getOwnerUuid(foxEntity);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        if (other == this) {
            return false;
        } else if (!(other instanceof FoxEntity otherFox)) {
            return false;
        } else if (((TamedFox)otherFox).isTamed(otherFox) != this.isTamed(foxEntity)) {
            return false;
        } else {
            return !otherFox.isSitting() && this.isInLove() && otherFox.isInLove();
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack){
        return TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem());
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    public void addAiGoals(CallbackInfo ci) {
        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> (entity instanceof ChickenEntity || entity instanceof RabbitEntity) && !isTamed(foxEntity));
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test(entity) && !isTamed(foxEntity));
        foxEntity.followFishGoal = new ActiveTargetGoal<>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && !isTamed(foxEntity));
        foxEntity.goalSelector.add(1, new FoxSitGoal(foxEntity));
        foxEntity.goalSelector.add(1, new FoxAttackWithOwnerGoal(foxEntity));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks()), false));

        foxEntity.goalSelector.getGoals().removeIf((goal) -> goal.getGoal() instanceof FleeEntityGoal<?>);
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(this, PolarBearEntity.class, 8.0F, 1.6, 1.4, entity -> !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !this.isTamed(foxEntity)
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(6, new FoxFollowPlayerGoal(foxEntity, 1.0, 10.0f, 2.0f, false));
    }

    @Unique
    public void setFoxOwner(PlayerEntity player){
        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        foxEntity.getDataTracker().set(OWNER, Optional.of(player.getUuid()));
        if (player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity)player, this);
        }
    }

    @Override
    public EntityView method_48926() {
        if (foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        return foxEntity.getEntityWorld();
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