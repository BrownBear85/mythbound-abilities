package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AbilityBinding;
import zone.bonker.mythbound_core.data.EntityAbilities;

import javax.annotation.Nullable;

public record C2SSetBindingPacket(ResourceLocation abilityId, @Nullable AbilityBinding binding) implements CustomPacketPayload {
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
            EntityAbilities.setBind(context.player(), abilityId, binding);
        });
    }
}
