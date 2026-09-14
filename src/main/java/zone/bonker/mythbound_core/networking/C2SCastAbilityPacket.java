package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.data.CharacterBuild;

public record C2SCastAbilityPacket(ResourceLocation abilityId, int targetId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SCastAbilityPacket> TYPE = new CustomPacketPayload.Type<>(MythboundCore.identifier("c2s_cast_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCastAbilityPacket> CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, C2SCastAbilityPacket::abilityId,
                    ByteBufCodecs.VAR_INT, C2SCastAbilityPacket::targetId,
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
                MythboundCore.LOGGER.warn("Player {} tried to cast an unknown ability {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (!CharacterBuild.get(context.player()).hasAbility(abilityId)) {
                MythboundCore.LOGGER.warn("Player {} tried to cast {} but they haven't unlocked it", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (!ability.type().isCastable() || !ability.cost().canCast(context.player())) {
                MythboundCore.LOGGER.warn("Player {} tried to cast {} without the required magic units", context.player().getScoreboardName(), abilityId);
                return;
            }

            LivingEntity target = null;
            if (!ability.targeting().isEmpty()) {
                if (targetId == -1) {
                    if (ability.targeting().targetRequired()) {
                        MythboundCore.LOGGER.warn("Player {} tried to cast {} without a target", context.player().getScoreboardName(), abilityId);
                        return;
                    }
                } else {
                    if (serverLevel.getEntity(targetId) instanceof LivingEntity livingTarget) {
                        if (!ability.targeting().matches(livingTarget, context.player())) {
                            MythboundCore.LOGGER.warn("Player {} tried to cast {} with an invalid mob type as the target", context.player().getScoreboardName(), abilityId);
                            return;
                        }
                        target = livingTarget;
                    } else {
                        MythboundCore.LOGGER.warn("Player {} tried to cast {} with an invalid target id", context.player().getScoreboardName(), abilityId);
                        return;
                    }
                }
            }

            ability.cost().consumeUnits(context.player());
            ability.onCast(context.player(), target);

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), new S2CEntityAbilityPacket(context.player().getId(), abilityId, targetId));
        });
    }
}
