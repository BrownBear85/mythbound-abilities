package zone.bonker.mythbound_core.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Subclass;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.CharacterBuild;

public record C2SUnlockAbilityPacket(ResourceLocation abilityId, boolean mainAbilityTree) implements CustomPacketPayload {
    public static final Type<C2SUnlockAbilityPacket> TYPE = new Type<>(MythboundCore.identifier("c2s_unlock_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SUnlockAbilityPacket> CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, C2SUnlockAbilityPacket::abilityId,
                    ByteBufCodecs.BOOL, C2SUnlockAbilityPacket::mainAbilityTree,
                    C2SUnlockAbilityPacket::new
            );

    @Override
    public Type<C2SUnlockAbilityPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability == null) {
                MythboundCore.LOGGER.error("{} tried to unlock an ability that doesn't exist: {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            CharacterBuild characterBuild = CharacterBuild.get(context.player());
            CharacterClass characterClass = characterBuild.getCharacterClass();
            if (characterClass == null) {
                MythboundCore.LOGGER.error("{} tried to unlock an ability before selecting a class", context.player().getScoreboardName());
                return;
            }

            AbilityTree abilityTree;
            if (mainAbilityTree) {
                abilityTree = characterClass.mainAbilityTree();
            } else {
                Subclass subclass = characterBuild.getSubclass();
                if (subclass == null) {
                    MythboundCore.LOGGER.error("{} tried to unlock an ability from their subclass before selecting a subclass", context.player().getScoreboardName());
                    return;
                }
                abilityTree = subclass.abilityTree();
            }

            AbilityTree.Node node = abilityTree.getNodeForAbility(abilityId);
            if (node == null) {
                MythboundCore.LOGGER.error("{} tried to unlock an ability that is not present on the specified ability tree: {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (node.requiredAbilities().stream().anyMatch(requiredId -> !characterBuild.hasAbility(requiredId))) {
                MythboundCore.LOGGER.error("{} tried to unlock an ability before unlocking all of its requirement abilities: {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            if (node.cost() > (mainAbilityTree ? characterBuild.getClassUnlockPoints() : characterBuild.getSubclassUnlockPoints())) {
                MythboundCore.LOGGER.error("{} tried to unlock an ability that they cannot afford: {}", context.player().getScoreboardName(), abilityId);
                return;
            }

            characterBuild.unlockAbilityFromTree(abilityTree, node, mainAbilityTree);
        });
    }
}