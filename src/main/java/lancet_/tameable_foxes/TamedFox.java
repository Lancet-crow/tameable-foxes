package lancet_.tameable_foxes;

import com.google.common.base.Objects;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

import static net.minecraft.entity.passive.FoxEntity.OWNER;

public interface TamedFox {
    default boolean isTamed(FoxEntity foxEntity){
        return getOwnerUuid(foxEntity) != null;
    }

    default UUID getOwnerUuid(FoxEntity foxEntity){
        return foxEntity.getDataTracker().get(OWNER).orElse(null);
    }

    default PlayerEntity getOwner(FoxEntity foxEntity){
        UUID uUID = getOwnerUuid(foxEntity);
        return uUID == null ? null : foxEntity.getEntityWorld().getPlayerByUuid(uUID);
    }

    default boolean isOwner(FoxEntity foxEntity, PlayerEntity player) {return Objects.equal(getOwnerUuid(foxEntity), (player.getUuid()));}
}
