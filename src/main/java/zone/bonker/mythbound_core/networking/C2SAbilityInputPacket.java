package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.data.SpellData;

public record C2SAbilityInputPacket(ResourceLocation abilityId, int targetId, byte action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SAbilityInputPacket> TYPE = new CustomPacketPayload.Type<>(MythboundCore.identifier("c2s_ability_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAbilityInputPacket> CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, C2SAbilityInputPacket::abilityId,
                    ByteBufCodecs.VAR_INT, C2SAbilityInputPacket::targetId,
                    ByteBufCodecs.BYTE, C2SAbilityInputPacket::action,
                    C2SAbilityInputPacket::new
            );

    public static final byte START_HOLDING_OR_INSTANT_CAST = 0;
    public static final byte RELEASE = 1;
    public static final byte UPDATE_TARGET = 2;

    @Override
    public CustomPacketPayload.Type<C2SAbilityInputPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level() instanceof ServerLevel serverLevel)) {
                return;
            }

            SpellData spellData = SpellData.get(context.player());

            if (action == RELEASE) {
                if (!spellData.isCastingSpell()) {
                    MythboundCore.LOGGER.warn("Player {} tried to stop spell casting when they weren't casting a spell", context.player().getScoreboardName());
                } else {
                    spellData.stopCastingSpell(context.player().level().getGameTime());
                }
                return;
            }

            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability == null) {
                MythboundCore.LOGGER.warn("Player {} referenced an unknown ability {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (action == START_HOLDING_OR_INSTANT_CAST && !CharacterBuild.get(context.player()).hasAbility(abilityId)) {
                MythboundCore.LOGGER.warn("Player {} tried to cast {} but they haven't unlocked it", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (action == START_HOLDING_OR_INSTANT_CAST && (!ability.type().isCastable() || !ability.cost().canCast(context.player()) || spellData.isOnCooldown(abilityId))) {
                MythboundCore.LOGGER.warn("Player {} tried to cast {} at an invalid time", context.player().getScoreboardName(), abilityId);
                return;
            }

            LivingEntity target = null;
            if ((action == START_HOLDING_OR_INSTANT_CAST || action == UPDATE_TARGET) && !ability.targeting().isEmpty()) {
                if (targetId == -1) {
                    if (action == START_HOLDING_OR_INSTANT_CAST
                            ? ability.spellBehavior().requiresTargetForInitialInput()
                            : ability.targeting().targetRequiredForCast()) {
                        spellData.stopCastingSpell(context.player().level().getGameTime());
                        return;
                    }
                } else {
                    if (serverLevel.getEntity(targetId) instanceof LivingEntity livingTarget) {
                        if (!ability.targeting().matches(livingTarget, context.player())) {
                            MythboundCore.LOGGER.warn("Player {} tried to cast {} with an invalid mob type as the target", context.player().getScoreboardName(), abilityId);
                            spellData.stopCastingSpell(context.player().level().getGameTime());
                            return;
                        }
                        target = livingTarget;
                    } else {
                        MythboundCore.LOGGER.warn("Player {} tried to cast {} with an invalid target id", context.player().getScoreboardName(), abilityId);
                        spellData.stopCastingSpell(context.player().level().getGameTime());
                        return;
                    }
                }
            }

            if (action == START_HOLDING_OR_INSTANT_CAST) {
                spellData.startHoldingOrInstantCast(ability, target, context.player().level().getGameTime());
            } if (action == UPDATE_TARGET) {
                spellData.setTarget(target);
            }
        });
    }
}
