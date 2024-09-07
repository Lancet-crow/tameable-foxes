package lancet_.tameable_foxes;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableFoxes implements ModInitializer {
	public static final String MOD_ID = "tameable-foxes";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Foxes becoming tameable...");
	}

	public static void addFoxGoals(FoxEntity entity) {
		if (FoxEntity.OWNER != null) {
			entity.goalSelector.add(1, new FoxSitGoal(entity));
			entity.goalSelector.add(1, new FoxAttackWithOwnerGoal(entity));
			entity.goalSelector.add(6, new FoxFollowPlayerGoal(entity, 1.0, 10.0f, 2.0f));
			entity.goalSelector.add(4, new FleeEntityGoal<PlayerEntity>(entity,
					PlayerEntity.class, 16.0f, 1.6, 1.4, e -> !e.isSneaky()
					&& !EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e) && !entity.canTrust(e.getUuid())
					&& !entity.isAggressive()));
			entity.goalSelector.add(2, new TemptGoal(entity, 0.75,
					Ingredient.ofStacks(new ItemStack(Items.SWEET_BERRIES)), false));
		} else {
			entity.goalSelector.add(0, new PowderSnowJumpGoal(entity, entity.getWorld()));
			entity.goalSelector.add(1, entity.new StopWanderingGoal());
			entity.goalSelector.add(1, entity.new EscapeWhenNotAggressiveGoal(2.2));
			entity.goalSelector.add(5, entity.new MoveToHuntGoal());
			entity.goalSelector.add(7, entity.new DelayedCalmDownGoal());
			entity.goalSelector.add(8, entity.new FollowParentGoal(entity, 1.25));
			entity.goalSelector.add(9, entity.new GoToVillageGoal(32, 200));
			entity.goalSelector.add(10, entity.new EatBerriesGoal((double) 1.2f, 12, 1));
			entity.goalSelector.add(13, entity.new SitDownAndLookAroundGoal());
			entity.goalSelector.add(6, entity.new AvoidDaylightGoal(1.25));
		}
	}
}