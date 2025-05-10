package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.MappingUtil;
import lancet_.tameable_foxes.ReflectionUtil;
import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.TameableTricksInterface;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin extends AnimalEntity {
    @Shadow protected abstract void eat(PlayerEntity player, Hand hand, ItemStack stack);

    @Shadow public abstract boolean isSitting();

    @Shadow public abstract void setSitting(boolean sitting);

    @Shadow public abstract boolean isBreedingItem(ItemStack stack);

    @Shadow public abstract boolean isSleeping();

    @Shadow public abstract void setSleeping(boolean sleeping);

    protected FoxEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();

        if (this.getWorld().isClient) {
            boolean bl = this.tameable$isOwner(player) || this.tameable$isTamed() ||
                    (isTamingItem(item) && !this.tameable$isTamed()) || isBreedingItem(itemStack);
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        } else if (this.tameable$isTamed()) {
            if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                this.heal((float) (item.getFoodComponent() != null ? item.getFoodComponent().getHunger() : 0));
                return ActionResult.SUCCESS;
            } else {
                ActionResult actionResult = super.interactMob(player, hand);
                if ((!actionResult.isAccepted() || this.isBaby()) && this.tameable$isOwner(player)) {
                    if (this.isSleeping()){
                        this.setSleeping(false);
                    }
                    else{
                        this.setSitting(!this.isSitting());
                    }
                    this.jumping = false;
                    this.getNavigation().stop();
                    this.setTarget(null);
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
                this.tameable$setOwner(player);
                this.navigation.stop();
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

    @Inject(method = "setSitting", at = @At("TAIL"))
    public void tameable_foxes$setSitting(boolean sitting, CallbackInfo ci){
        if (((TameableEntity)(Object)this) instanceof TameableTricksInterface tameableTricksInterface){
            tameableTricksInterface.setSittingValue(sitting);
        }
    }

    @Unique
    private boolean isTamingItem(Item item){
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(item);
    }

    @Inject(method = "isBreedingItem", at = @At("RETURN"), cancellable = true)
    public void tameable_foxes$isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem()));
    }

    @Unique
    private boolean tameable$isOwner(PlayerEntity player) {
        return tameable$invoke(
                "method_6171",
                "(%s)Z".formatted(MappingUtil.definitionToIntermediaryBytecodeName(LivingEntity.class)),
                boolean.class,
                new Class[]{LivingEntity.class},
                player
        );
    }

    @Unique
    private void tameable$setSitting(boolean sitting) {
        tameable$invoke("method_24346", "(Z)V", new Class[]{boolean.class}, sitting);
    }

    @Unique
    private boolean tameable$isSitting() {
        return tameable$invoke("method_24345", "()Z", boolean.class);
    }

    @Unique
    private boolean tameable$isTamed() {
        return tameable$invoke("method_6181", "()Z", boolean.class);
    }

    @Unique
    private void tameable$setOwner(PlayerEntity player) {
        tameable$invoke(
                "method_6170",
                "(%s)V".formatted(MappingUtil.definitionToIntermediaryBytecodeName(PlayerEntity.class)),
                new Class[]{PlayerEntity.class},
                player
        );
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    public void addAiGoals(CallbackInfo ci) {
        FoxEntity foxEntity = (FoxEntity) (Object) this;

        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> (entity instanceof ChickenEntity || entity instanceof RabbitEntity) && !this.tameable$isTamed());
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test(entity) && !this.tameable$isTamed());
        foxEntity.followFishGoal = new ActiveTargetGoal<>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && !this.tameable$isTamed());
        foxEntity.goalSelector.add(1, new SitGoal((TameableEntity) (Object) this));
        foxEntity.goalSelector.add(1, new AttackWithOwnerGoal((TameableEntity) (Object) this));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks()), true));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !this.tameable$isTamed()
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(6, new FollowOwnerGoal((TameableEntity) (Object) this, 1.0, 10.0f, 2.0f, false));
    }

    @Unique
    private <T> T tameable$invoke(String methodName, String desc, Class<T> returnType, Class<?>[] parameterTypes, Object... args) {
        return ReflectionUtil.invoke(this, "net.minecraft.class_1321", methodName, desc, returnType, parameterTypes, args);
    }

    @Unique
    private <T> T tameable$invoke(String methodName, String desc, Class<T> returnType) {
        return tameable$invoke(methodName, desc, returnType, new Class[0]);
    }

    @Unique
    private void tameable$invoke(String methodName, String desc, Class<?>[] parameterTypes, Object... args) {
        tameable$invoke(methodName, desc, void.class, parameterTypes, args);
    }
}