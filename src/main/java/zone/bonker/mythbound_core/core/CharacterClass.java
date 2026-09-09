package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.core.ability.component.AbilityComponent;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.List;
import java.util.Map;

public record CharacterClass(Component name, List<Component> description, AttributeList attributes, List<AbilityComponent> components,
                             AbilityTree mainAbilityTree, Map<ResourceLocation, Subclass> subclasses, List<ResourceLocation> possibleRaceIds, ModelProperties modelProperties)
        implements NamedAndDescribed, ModelProperties.ModelPropContainer {

    public static final Codec<CharacterClass> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(CharacterClass::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(CharacterClass::description),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(CharacterClass::attributes),
            AbilityComponent.DIRECT_CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(CharacterClass::components),
            AbilityTree.CODEC.fieldOf("main_ability_tree").forGetter(CharacterClass::mainAbilityTree),
            Codec.unboundedMap(ResourceLocation.CODEC, Subclass.CODEC).fieldOf("subclasses").forGetter(CharacterClass::subclasses),
            ResourceLocation.CODEC.listOf().optionalFieldOf("possible_races", List.of()).forGetter(CharacterClass::possibleRaceIds),
            ModelProperties.CODEC.optionalFieldOf("model_properties", ModelProperties.DEFAULT).forGetter(CharacterClass::modelProperties)
    ).apply(inst, CharacterClass::new));

    @Override
    public ResourceLocation getId() {
        return MythboundCore.CLASSES.getData().inverse().get(this);
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.CLASS);
        mainAbilityTree.initialize(entity);
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.CLASS);
        mainAbilityTree.deinitialize(entity);
    }
}
