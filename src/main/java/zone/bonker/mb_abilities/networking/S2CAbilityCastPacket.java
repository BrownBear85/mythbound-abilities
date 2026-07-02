package zone.bonker.mb_abilities.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.init.Abilities;

public record S2CAbilityCastPacket(int entityId, ResourceLocation abilityId) implements CustomPacketPayload {
    public static final Type<S2CAbilityCastPacket> TYPE = new Type<>(MythboundAbilities.identifier("s2c_ability_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CAbilityCastPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, S2CAbilityCastPacket::entityId,
                    ResourceLocation.STREAM_CODEC, S2CAbilityCastPacket::abilityId,
                    S2CAbilityCastPacket::new
            );

    @Override
    public Type<S2CAbilityCastPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            Ability ability = level.registryAccess().registryOrThrow(Abilities.ABILITIES_KEY).get(abilityId);
            if (ability == null) {
                return;
            }

            Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            ability.castClient(level, livingEntity);
        });
    }
}
