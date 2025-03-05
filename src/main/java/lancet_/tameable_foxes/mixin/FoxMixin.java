package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxes;
import lancet_.tameable_foxes.goals.FoxAttackWithOwnerGoal;
import lancet_.tameable_foxes.goals.FoxFollowPlayerGoal;
import lancet_.tameable_foxes.goals.FoxSitGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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
public class FoxMixin extends Animal {

    @Unique
    Fox fox;
    public FoxMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.fox = (Fox) (Object) entityType;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FOX_FOOD);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        if (this.fox == null) this.fox = (Fox) (Object) this;
        return this.fox.getBreedOffspring(level, otherParent);
    }

    @Override
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (this.fox == null) this.fox = (Fox) (Object) this;
        UUID ownerUUID = this.fox.getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
        InteractionResult interactionResult = super.mobInteract(player, hand);
        if (interactionResult.consumesAction()){
            if (ownerUUID == null && TameableFoxes.CONFIG.foxesTameDirectly()){
                setFoxOwner(player.getUUID());
            }
            return interactionResult;
        }
        if (!player.getUUID().equals(ownerUUID)) return InteractionResult.PASS;
        this.fox.setSitting(!this.fox.isSitting());
        this.jumping = false;
        this.navigation.stop();
        this.setTarget(null);
        return InteractionResult.SUCCESS;
    }

    @Inject(method = "registerGoals", at = @At(value = "HEAD"), cancellable = true)
    public void addAiGoals(CallbackInfo ci){
        if(this.fox == null) this.fox = (Fox) (Object) this;
        this.fox.landTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, Animal.class, 10, false, false,
                entity -> entity instanceof Chicken || entity instanceof Rabbit);
        this.fox.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
        this.fox.fishTargetGoal = new NearestAttackableTargetGoal<>(
                this.fox, AbstractFish.class, 20, false, false,
                entity -> entity instanceof AbstractSchoolingFish);
        this.fox.goalSelector.addGoal(0, this.fox.new FoxFloatGoal());
        this.fox.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.fox.goalSelector.addGoal(1, this.fox.new FaceplantGoal());
        this.fox.goalSelector.addGoal(2, this.fox.new FoxPanicGoal(2.2));
        this.fox.goalSelector.addGoal(3, this.fox.new FoxBreedGoal(1.0));
        this.fox.goalSelector.addGoal(5, this.fox.new StalkPreyGoal());
        this.fox.goalSelector.addGoal(6, this.fox.new SeekShelterGoal(1.25));
        this.fox.goalSelector.addGoal(7, this.fox.new FoxMeleeAttackGoal(1.2F, true));
        this.fox.goalSelector.addGoal(7, this.fox.new SleepGoal());
        this.fox.goalSelector.addGoal(8, this.fox.new FoxFollowParentGoal(this.fox, 1.25));
        this.fox.goalSelector.addGoal(9, this.fox.new FoxStrollThroughVillageGoal(32, 200));
        this.fox.goalSelector.addGoal(10, this.fox.new FoxEatBerriesGoal(1.2F, 12, 1));
        this.fox.goalSelector.addGoal(10, new LeapAtTargetGoal(this.fox, 0.4F));
        this.fox.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this.fox, 1.0));
        this.fox.goalSelector.addGoal(11, this.fox.new FoxSearchForItemsGoal());
        this.fox.goalSelector.addGoal(12, this.fox.new FoxLookAtPlayerGoal(this.fox, Player.class, 24.0F));
        this.fox.goalSelector.addGoal(13, this.fox.new PerchAndSearchGoal());
        this.fox.goalSelector.addGoal(10, this.fox.new FoxPounceGoal());
        this.fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(this.fox, Wolf.class, 8.0F,
                1.6, 1.4, entity -> !((Wolf)entity).isTame() && !this.fox.isDefending()));
        this.fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(this.fox, PolarBear.class, 8.0F,
                1.6, 1.4, entity -> !this.fox.isDefending()));
        this.fox.targetSelector.addGoal(3, this.fox.new DefendTrustedTargetGoal(LivingEntity.class, false,
                        false, player -> Fox.TRUSTED_TARGET_SELECTOR.test(player)
                && !this.fox.trusts(player.getUUID())));
        this.fox.goalSelector.addGoal(1, new FoxAttackWithOwnerGoal(this.fox));
        this.fox.goalSelector.addGoal(6, new FoxFollowPlayerGoal(this.fox, 1.0, 10.0f, 2.0f));
        this.fox.goalSelector.addGoal(1, new FoxSitGoal(this.fox));
        this.fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(this.fox,
                Player.class, 16.0F, 1.6, 1.4,e -> !e.isShiftKeyDown()
                && Fox.AVOID_PLAYERS.test(e) && !this.fox.trusts(e.getUUID()) && !this.fox.isDefending()));
        this.fox.goalSelector.addGoal(1, new TemptGoal(this.fox, 0.75,
                Ingredient.of(new ItemStack(Items.SWEET_BERRIES)), false));
        ci.cancel();
    }

    @Unique
    public void setFoxOwner(UUID newOwnerUUID) {
        assert this.fox != null;
        this.fox.getEntityData().set(DATA_TRUSTED_ID_0, Optional.of(newOwnerUUID));
    }
}
