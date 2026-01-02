package lancet_.tameable_foxes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lancet_.tameable_foxes.TameableFoxesConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public class MobEntityMixin {
    @WrapOperation(method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;canEntityGrief(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean checkFoxIgnoreMobGriefingRule(Level level, Entity entity, Operation<Boolean> original) {
        if (entity instanceof Fox && TameableFoxesConfig.config.foxesCanIgnoreMobGriefingRule) {
            return true;
        }
        return original.call(level, entity);
    }
}
