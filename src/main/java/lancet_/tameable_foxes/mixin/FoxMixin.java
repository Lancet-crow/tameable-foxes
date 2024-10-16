package lancet_.tameable_foxes.mixin;

import lancet_.tameable_foxes.TameableFoxes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;


@Mixin(Fox.class)
public abstract class FoxMixin extends TamableAnimal {
    protected FoxMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        FoxMixin fox = ((FoxMixin) (Object) this);
        ItemStack itemstack = player.getItemInHand(hand);
        if (fox.isTame()) {
            if (fox.isOwnedBy(player)) {
                if (fox.isFood(itemstack) && fox.getHealth() < fox.getMaxHealth()) {
                    if (!fox.level().isClientSide()) {
                        FoodProperties foodproperties = itemstack.getFoodProperties(fox);
                        fox.heal(foodproperties != null ? (float)foodproperties.nutrition() : 1.0F);
                        fox.usePlayerItem(player, hand, itemstack);
                    }

                    return InteractionResult.sidedSuccess(fox.level().isClientSide());
                }

                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction()) {
                    fox.setOrderedToSit(!fox.isOrderedToSit());
                    return InteractionResult.sidedSuccess(fox.level().isClientSide());
                }

                return interactionresult;
            }
        } else if (this.isFood(itemstack)) {
            if (!fox.level().isClientSide()) {
                fox.usePlayerItem(player, hand, itemstack);
                fox.tryToTame(player);
                fox.setPersistenceRequired();
            }

            return InteractionResult.sidedSuccess(fox.level().isClientSide());
        }

        InteractionResult interactionresult1 = super.mobInteract(player, hand);
        if (interactionresult1.consumesAction()) {
            fox.setPersistenceRequired();
        }

        return interactionresult1;
    }
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES);
    }
    @Unique
    private void tryToTame(Player player) {
        FoxMixin fox = ((FoxMixin) (Object) this);
        if (fox.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(fox, player)) {
            fox.tame(player);
            fox.setOrderedToSit(true);
            fox.level().broadcastEntityEvent(fox, (byte)7);
            TameableFoxes.LOGGER.info("Result of taming: True");
        } else {
            fox.level().broadcastEntityEvent(fox, (byte)6);
            TameableFoxes.LOGGER.info("Result of taming: False");
        }
    }
}