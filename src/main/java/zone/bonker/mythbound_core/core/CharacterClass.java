package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability_effect.MythboundEffect;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.List;

public record CharacterClass(Component name, List<Component> description, AttributeList attributes, List<MythboundEffect> effects,
                             AbilityTree abilityTree, List<ResourceLocation> possibleRaceIds) implements NamedAndDescribed {

    public static final Codec<CharacterClass> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(CharacterClass::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(CharacterClass::description),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(CharacterClass::attributes),
            MythboundEffect.DIRECT_CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(CharacterClass::effects),
            AbilityTree.CODEC.fieldOf("ability_tree").forGetter(CharacterClass::abilityTree),
            ResourceLocation.CODEC.listOf().optionalFieldOf("possible_races", List.of()).forGetter(CharacterClass::possibleRaceIds)
    ).apply(inst, CharacterClass::new));

    public ResourceLocation getId() {
        return MythboundCore.CLASSES.getData().inverse().get(this);
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.CLASS);
        abilityTree.initialize();
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.CLASS);
        abilityTree.deinitialize();
    }
}
