package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability_effect.MythboundEffect;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.List;

public record Race(Component name, List<Component> description, AttributeList attributes, List<MythboundEffect> effects,
                   List<Ability> inherentAbilities, List<CharacterClass> possibleClasses, ModelProperties modelProperties)
        implements NamedAndDescribed, ModelProperties.ModelPropContainer {

    public static final Codec<Race> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(Race::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(Race::description),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(Race::attributes),
            MythboundEffect.DIRECT_CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(Race::effects),
            MythboundSerialization.registryCodec(() -> MythboundCore.ABILITIES).listOf().optionalFieldOf("inherent_abilities", List.of()).forGetter(Race::inherentAbilities),
            MythboundSerialization.registryCodec(() -> MythboundCore.CLASSES).listOf().fieldOf("possible_classes").forGetter(Race::possibleClasses),
            ModelProperties.CODEC.optionalFieldOf("model_properties", ModelProperties.DEFAULT).forGetter(Race::modelProperties)
    ).apply(inst, Race::new));

    @Override
    public ResourceLocation getId() {
        return MythboundCore.RACES.getData().inverse().get(this);
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.RACE);

        CharacterBuild data = CharacterBuild.get(entity);
        for (Ability ability : inherentAbilities) {
            data.unlockAbility(ability);
        }

        if (modelProperties.hasCustomHitbox()) {
            CharacterBuild.refreshDimensions(entity);
        }
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.RACE);

        CharacterBuild data = CharacterBuild.get(entity);
        for (Ability ability : inherentAbilities) {
            data.removeAbility(ability);
        }

        if (modelProperties.hasCustomHitbox()) {
            CharacterBuild.refreshDimensions(entity);
        }
    }
}
