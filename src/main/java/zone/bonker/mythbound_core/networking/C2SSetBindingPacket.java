package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityBinding;
import zone.bonker.mythbound_core.data.CharacterBuild;

public record C2SSetBindingPacket(ResourceLocation abilityId, AbilityBinding binding) implements CustomPacketPayload {
    public static final Type<C2SSetBindingPacket> TYPE = new Type<>(MythboundCore.identifier("c2s_set_binding"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetBindingPacket> CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, C2SSetBindingPacket::abilityId,
                    AbilityBinding.NETWORK_CODEC, C2SSetBindingPacket::binding,
                    C2SSetBindingPacket::new
            );

    @Override
    public Type<C2SSetBindingPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability == null) {
                MythboundCore.LOGGER.warn("Player {} tried set a binding for an unknown ability {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (!CharacterBuild.get(context.player()).hasAbility(abilityId)) {
                MythboundCore.LOGGER.warn("Player {} tried to set a binding for {} but they haven't unlocked it", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (!ability.type().isCastable()) {
                MythboundCore.LOGGER.warn("Player {} tried to set a binding for an ability that isn't castable: {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            CharacterBuild.get(context.player()).setAbilityBinding(abilityId, binding);
        });
    }
}
