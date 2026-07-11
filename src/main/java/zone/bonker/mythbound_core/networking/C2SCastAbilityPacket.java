package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.data.CharacterBuild;

public record C2SCastAbilityPacket(ResourceLocation abilityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SCastAbilityPacket> TYPE = new CustomPacketPayload.Type<>(MythboundCore.identifier("c2s_cast_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCastAbilityPacket> CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, C2SCastAbilityPacket::abilityId,
                    C2SCastAbilityPacket::new
            );

    @Override
    public CustomPacketPayload.Type<C2SCastAbilityPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability == null) {
                MythboundCore.LOGGER.warn("Player {} tried to cast an unregistered ability {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (!CharacterBuild.get(context.player()).hasAbility(abilityId)) {
                MythboundCore.LOGGER.warn("Player {} tried to cast an ability that they don't have unlocked", context.player().getScoreboardName());
                return;
            }

            ability.tryCast(context.player());
        });
    }
}
