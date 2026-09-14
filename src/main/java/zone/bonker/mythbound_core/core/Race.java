package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.List;

public record Race(Component name, List<Component> description, AttributeList attributes,
                   List<Ability> inherentAbilities, List<CharacterClass> possibleClasses, ModelProperties modelProperties)
        implements NamedAndDescribed, ModelProperties.ModelPropContainer {

    public static final Codec<Race> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(Race::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(Race::description),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(Race::attributes),
            MythboundSerialization.registryCodec(() -> MythboundCore.ABILITIES).listOf().optionalFieldOf("inherent_abilities", List.of()).forGetter(Race::inherentAbilities),
            MythboundSerialization.registryCodec(() -> MythboundCore.CLASSES).listOf().fieldOf("possible_classes").forGetter(Race::possibleClasses),
            ModelProperties.CODEC.optionalFieldOf("model_properties", ModelProperties.DEFAULT).forGetter(Race::modelProperties)
    ).apply(inst, Race::new));

    @Override
    public ResourceLocation getId() {
        return MythboundCore.RACES.getKeyOrThrow(this);
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.RACE);

        for (Ability ability : inherentAbilities) {
            ability.initialize(entity);
        }
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.RACE);

        for (Ability ability : inherentAbilities) {
            ability.deinitialize(entity);
        }
    }
}
