package lancet_.tameable_foxes.mixin.compat;

import lancet_.tameable_foxes.TamedFox;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AnimalEntity.class)
public class CalmDownDogMixin {
    @Inject(method="interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cil) {
        World world = player.getWorld();
        AnimalEntity entity = ((AnimalEntity)(Object)this);
        if (entity instanceof FoxEntity foxEntity){
            UUID owner = ((TamedFox)foxEntity).getOwnerUuid(foxEntity);
            if (player.isSneaking() && owner != null) {
                foxEntity.getEntityWorld().sendEntityStatus(foxEntity, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                if (owner.equals(player.getUuid())) {
                    foxEntity.setAggressive(false);
                    foxEntity.setTarget(null);
                    foxEntity.setAttacker(null);
                    foxEntity.getNavigation().stop();
                    if (foxEntity.goalSelector.getRunningGoals().anyMatch(goal ->
                            goal.getGoal() instanceof TrackTargetGoal) && !world.isClient()){
                        foxEntity.goalSelector.getRunningGoals().forEachOrdered(goal -> {
                            if (goal.getGoal() instanceof TrackTargetGoal trackTargetGoal){
                                trackTargetGoal.stop();
                            }
                        });
                    }
                }

                player.swingHand(Hand.MAIN_HAND, !world.isClient);
                cil.setReturnValue(ActionResult.CONSUME);
                cil.cancel();
            }
        }
    }
}
