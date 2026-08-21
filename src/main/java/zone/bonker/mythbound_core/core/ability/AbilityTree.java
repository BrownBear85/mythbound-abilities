package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.CharacterBuild;

import javax.annotation.Nullable;
import java.util.List;

public record AbilityTree(List<Node> nodes) {
    public static final Codec<AbilityTree> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Node.CODEC.listOf().fieldOf("nodes").forGetter(AbilityTree::nodes)
    ).apply(inst, AbilityTree::new));

    @Nullable
    public Node getNodeForAbility(ResourceLocation abilityId) {
        for (Node node : nodes) {
            if (node.abilityId.equals(abilityId)) {
                return node;
            }
        }
        return null;
    }

    public void unlockAllFreeAbilities(CharacterBuild characterBuild) {
        for (Node node : nodes) {
            if (node.cost > 0) {
                continue;
            }

            Ability ability = MythboundCore.ABILITIES.getData().get(node.abilityId);
            if (ability == null) {
                continue;
            }

            if (node.requiredAbilities.stream().anyMatch(requiredId -> !characterBuild.hasAbility(requiredId))) {
                continue;
            }

            characterBuild.unlockAbility(ability);
        }
    }

    public void initialize(LivingEntity entity) {
        unlockAllFreeAbilities(CharacterBuild.get(entity));
    }

    public void deinitialize(LivingEntity entity) {
        CharacterBuild characterBuild = CharacterBuild.get(entity);
        for (Node node : nodes) {
            Ability ability = MythboundCore.ABILITIES.getData().get(node.abilityId);
            if (ability != null) {
                characterBuild.removeAbility(ability);
            }
        }
    }

    public record Node(ResourceLocation abilityId, List<ResourceLocation> requiredAbilities, int cost, int row, int column) {
        public static final Codec<Node> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("ability").forGetter(Node::abilityId),
                ResourceLocation.CODEC.listOf().optionalFieldOf("requires", List.of()).forGetter(Node::requiredAbilities),
                Codec.INT.optionalFieldOf("cost", 0).forGetter(Node::cost),
                Codec.INT.fieldOf("row").forGetter(Node::row),
                Codec.INT.fieldOf("column").forGetter(Node::column)
        ).apply(inst, Node::new));
    }
}
