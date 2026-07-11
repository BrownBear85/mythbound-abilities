package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;

public record S2CEntityAbilityPacket(int entityId, ResourceLocation abilityId) implements CustomPacketPayload {
    public static final Type<S2CEntityAbilityPacket> TYPE = new Type<>(MythboundCore.identifier("s2c_ability_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CEntityAbilityPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, S2CEntityAbilityPacket::entityId,
                    ResourceLocation.STREAM_CODEC, S2CEntityAbilityPacket::abilityId,
                    S2CEntityAbilityPacket::new
            );

    @Override
    public Type<S2CEntityAbilityPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability == null) {
                return;
            }

            Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            ability.cast(livingEntity);
        });
    }
}
