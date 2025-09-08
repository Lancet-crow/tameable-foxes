package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.goals.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0;

@Mixin(Fox.class)
public class FoxMixin extends Animal implements OwnableEntity {

    @Unique
    Fox fox;
    public FoxMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.fox = (Fox) (Object) entityType;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return TameableFoxesConfig.FOX_BREEDING_ITEMS.contains(stack.getItem());
    }

    @Override
    public boolean canMate(@NotNull Animal otherAnimal) {
        if (otherAnimal instanceof Fox otherFox && otherAnimal != this.fox){
            boolean isOtherFoxTamed = otherFox.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null) != null;
            if (isTame() == isOtherFoxTamed){
                return !fox.isSitting() && this.isInLove() && fox.isInLove();
            }
        }
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        Fox fox = EntityType.FOX.create(level);
        if (fox != null) {
            fox.setVariant(this.random.nextBoolean() ? this.fox.getVariant() : ((Fox)otherParent).getVariant());
            if (this.getOwner() != null){
                fox.getEntityData().set(DATA_TRUSTED_ID_0, Optional.ofNullable(this.getOwnerUUID()));
            }
        }
        return fox;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (this.fox == null) this.fox = (Fox) (Object) this;
        UUID ownerUUID = this.fox.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        InteractionResult interactionResult = super.mobInteract(player, hand);
        ItemStack itemStack = player.getItemInHand(hand);
        if (ownerUUID == null && isTamingItem(itemStack) && (TameableFoxesConfig.config.foxesTameDirectly || !isFood(itemStack))){
            itemStack.consume(1, player);
            fox.setInLove(player);
            setFoxOwner(player.getUUID());
            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }
            else{
                return InteractionResult.SUCCESS;
            }
        }
        else if (isFood(itemStack)){
            return interactionResult;
        }
        if (!player.getUUID().equals(ownerUUID)) return InteractionResult.PASS;
        if (fox.isSleeping()){
            fox.setSleeping(false);
        }
        else{
            fox.setSitting(!fox.isSitting());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean handleLeashAtDistance(@NotNull Entity leashHolder, float distance) {
        if (fox.isSitting()) {
            if (distance > 10.0F) {
                this.dropLeash(true, true);
            }

            return false;
        } else {
            return super.handleLeashAtDistance(leashHolder, distance);
        }
    }

    @Unique
    private boolean isTamingItem(ItemStack itemStack){
        return TameableFoxesConfig.FOX_TAMING_ITEMS.contains(itemStack.getItem());
    }

    @Inject(method = "registerGoals", at = @At(value = "TAIL"))
    public void addAiGoals(CallbackInfo ci){
        if(this.fox == null) this.fox = (Fox) (Object) this;

        this.fox.goalSelector.getAvailableGoals().removeIf(goal ->
                goal.getGoal() instanceof Fox.SeekShelterGoal ||
                goal.getGoal() instanceof Fox.PerchAndSearchGoal
                );

        this.fox.landTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, Animal.class, 10, false, false,
                entity -> (entity instanceof Chicken || entity instanceof Rabbit) && !isTame());
        this.fox.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, Turtle.class, 10, false, false,
                e -> (e instanceof Turtle && e.isBaby() && !e.isInWater()) && !isTame());
        this.fox.fishTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, AbstractFish.class, 20, false, false,
                entity -> entity instanceof AbstractSchoolingFish && !isTame());
        this.fox.goalSelector.addGoal(1, new FoxSitGoal(this.fox));
        this.fox.goalSelector.addGoal(1, new FoxAttackWithOwnerGoal(this.fox));
        this.fox.goalSelector.addGoal(1, new TemptGoal(this.fox, 0.75,
                Ingredient.of(TameableFoxesConfig.getFoxTemptingItemStacks()), false));
        this.fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(this.fox,
                Player.class, 16.0F, 1.6, 1.4,e ->
                Fox.AVOID_PLAYERS.test(e) && !isTame() && !this.fox.isAggressive()));
        this.fox.goalSelector.addGoal(5, new FoxStalkPreyGoal(this.fox));
        this.fox.goalSelector.addGoal(6, new FoxFollowPlayerGoal(this.fox, 1.0, 10.0f, 2.0f));
        this.fox.goalSelector.addGoal(6, new FoxSeekShelterGoal(1.25, fox));
        this.fox.goalSelector.addGoal(13, new FoxPerchAndSearchGoal(this.fox));
    }

    @Unique
    public void setFoxOwner(UUID newOwnerUUID) {
        assert this.fox != null;
        this.fox.getEntityData().set(DATA_TRUSTED_ID_0, Optional.of(newOwnerUUID));
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.fox.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
    }

    @Unique
    public boolean isTame() {
        return getOwnerUUID() != null;
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        net.minecraft.network.chat.Component deathMessage = this.getCombatTracker().getDeathMessage();
        super.die(cause);

        if (this.dead) {
            if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
                this.getOwner().sendSystemMessage(deathMessage);
            }
        }
    }
}
