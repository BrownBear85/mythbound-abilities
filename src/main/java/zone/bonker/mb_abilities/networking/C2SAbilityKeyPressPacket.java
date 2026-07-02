package zone.bonker.mb_abilities.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.data.EntityAbilities;

public record C2SAbilityKeyPressPacket(byte index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SAbilityKeyPressPacket> TYPE = new CustomPacketPayload.Type<>(MythboundAbilities.identifier("c2s_ability_key_press"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAbilityKeyPressPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE, C2SAbilityKeyPressPacket::index,
                    C2SAbilityKeyPressPacket::new
            );

    @Override
    public CustomPacketPayload.Type<C2SAbilityKeyPressPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Ability ability = EntityAbilities.getAbilityInSlot(context.player(), index);
            if (ability != null) {
                ability.tryCast(serverLevel, context.player());
            }
        });
    }
}
