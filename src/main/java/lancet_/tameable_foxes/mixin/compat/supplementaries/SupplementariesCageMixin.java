package lancet_.tameable_foxes.mixin.compat.supplementaries;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMobContainerItem.class)
public class SupplementariesCageMixin {

    @Definition(id = "entity", local = @Local(type = Entity.class, name = "entity"))
    @Definition(id = "NeutralMob", type = NeutralMob.class)
    @Expression("entity instanceof NeutralMob")
    @ModifyExpressionValue(method = "useOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean pacifyUncagedFox(boolean original, @Local Entity entity){
        return original && !(entity instanceof Fox);
    }

    @Inject(method = "lambda$angerNearbyEntities$2", at = @At("HEAD"), cancellable = true)
    private static void pacifyNearbyFoxes(Player player, NeutralMob mob, CallbackInfo ci){
        if (mob instanceof Fox){
            ci.cancel();
        }
    }

    @WrapOperation(method = "doInteract",
            at = @At(value = "INVOKE",
                    target = "Lnet/mehvahdjukaar/supplementaries/common/items/AbstractMobContainerItem;" +
                            "angerNearbyEntities(Lnet/minecraft/world/entity/Entity;" +
                            "Lnet/minecraft/world/entity/player/Player;)V"))
    private void cancelAngerBecauseOfFoxes(AbstractMobContainerItem instance, Entity entity,
                                           Player player, Operation<Void> original){
        if (entity instanceof Fox){
            return;
        }
        original.call(instance, entity, player);
    }
}
