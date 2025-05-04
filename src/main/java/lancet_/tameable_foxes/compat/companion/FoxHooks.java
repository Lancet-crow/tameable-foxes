package lancet_.tameable_foxes.compat.companion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import snownee.companion.CompanionCommonConfig;

import static snownee.companion.Hooks.isInjured;

public interface FoxHooks {
    static boolean wantsToAttack(FoxEntity pet, LivingEntity enemy) {
        if (CompanionCommonConfig.petWontAttackWhenInjured && isInjured(pet)) {
            return !(enemy instanceof Monster) && !(enemy instanceof IronGolemEntity);
        } else {
            return true;
        }
    }
}
