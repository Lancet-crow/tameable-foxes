package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxesConfig;
import lancet_.tameable_foxes.fox_goals.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
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
            setFoxOwner(Optional.ofNullable(player.getUuid()));
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
        return !isSitting();
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

    @Inject(method = "initGoals", at = @At("HEAD"), cancellable = true)
    public void addAiGoals(CallbackInfo ci) {
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;
        foxEntity.followChickenAndRabbitGoal = new ActiveTargetGoal<>((
                foxEntity), AnimalEntity.class, 10, false,
                false, entity -> (entity instanceof ChickenEntity || entity instanceof RabbitEntity) && !isTamed());
        foxEntity.followBabyTurtleGoal = new ActiveTargetGoal<>(foxEntity, TurtleEntity.class,
                10, false, false,
                entity -> TurtleEntity.BABY_TURTLE_ON_LAND_FILTER.test(entity) && !isTamed());
        foxEntity.followFishGoal = new ActiveTargetGoal<>(foxEntity, FishEntity.class, 20,
                false, false, entity -> entity instanceof SchoolingFishEntity && !isTamed());
        foxEntity.goalSelector.add(0, foxEntity.new FoxSwimGoal());
        foxEntity.goalSelector.add(1, new FoxSitGoal(foxEntity));
        foxEntity.goalSelector.add(1, new FoxAttackWithOwnerGoal(foxEntity));
        foxEntity.goalSelector.add(1, new TemptGoal(foxEntity, 0.75,
                Ingredient.ofStacks(TameableFoxesConfig.getFoxTemptingItemStacks()), false));
        foxEntity.goalSelector.add(1, foxEntity.new StopWanderingGoal());
        foxEntity.goalSelector.add(2, foxEntity.new EscapeWhenNotAggressiveGoal(2.2));
        foxEntity.goalSelector.add(3, foxEntity.new MateGoal(1.0));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, WolfEntity.class, 8.0f,
                1.6, 1.4, entity -> !((WolfEntity) entity).isTamed() && !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity, PolarBearEntity.class,
                8.0f, 1.6, 1.4, entity -> !foxEntity.isAggressive()));
        foxEntity.goalSelector.add(4, new FleeEntityGoal<>(foxEntity,
                PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !isTamed()
                && !(foxEntity.isAggressive())));
        foxEntity.goalSelector.add(5, new FoxMoveToHuntGoal(foxEntity));
        foxEntity.goalSelector.add(6, foxEntity.new JumpChasingGoal());
        foxEntity.goalSelector.add(6, new FoxFollowPlayerGoal(foxEntity, 1.0, 10.0f, 2.0f, false));
        foxEntity.goalSelector.add(6, new FoxAvoidDaylightGoal(foxEntity, 1.25));
        foxEntity.goalSelector.add(7, foxEntity.new AttackGoal(1.2f, true));
        foxEntity.goalSelector.add(7, foxEntity.new DelayedCalmDownGoal());
        foxEntity.goalSelector.add(8, foxEntity.new FollowParentGoal(foxEntity, 1.25));
        foxEntity.goalSelector.add(9, foxEntity.new GoToVillageGoal(32, 200));
        foxEntity.goalSelector.add(10, foxEntity.new EatBerriesGoal(1.2F, 12, 1));
        foxEntity.goalSelector.add(10, new PounceAtTargetGoal(foxEntity, 0.4f));
        foxEntity.goalSelector.add(11, new WanderAroundFarGoal(foxEntity, 1.0));
        foxEntity.goalSelector.add(11, foxEntity.new PickupItemGoal());
        foxEntity.goalSelector.add(12, foxEntity.new LookAtEntityGoal(foxEntity, PlayerEntity.class, 24.0f));
        if(foxEntity == null) foxEntity = (FoxEntity) (Object) this;
        assert foxEntity != null;
        foxEntity.goalSelector.add(13, new FoxSitDownAndLookAroundGoal(foxEntity));
        ci.cancel();
    }
    @Unique
    public void setFoxOwner(Optional<UUID> newOwnerUUID){
        assert foxEntity != null;
        foxEntity.getDataTracker().set(OWNER, newOwnerUUID);
        //addAiGoals(new CallbackInfo("owningFox", true));
        assert MinecraftClient.getInstance().player != null;
        //MinecraftClient.getInstance().player.sendMessage(Text.of("Tamed fox to " + MinecraftClient.getInstance().player.getEntityName()));
    }
}