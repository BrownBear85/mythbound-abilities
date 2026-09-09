package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AttributeList;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.ability.component.AbilityComponent;
import zone.bonker.mythbound_core.data.MythboundSerialization;
import zone.bonker.mythbound_core.networking.S2CEntityAbilityPacket;

import java.util.List;

public final class Ability implements NamedAndDescribed {

    public static final Codec<Ability> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(o -> o.name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(o -> o.rawDescription),
            AbilityComponent.DIRECT_CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(o -> o.components),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(o -> o.attributes)
    ).apply(inst, Ability::new));

    private final Component name;
    private final List<Component> rawDescription;
    private final List<Component> description;
    private final List<AbilityComponent> components;
    private final AttributeList attributes;
    private ResourceLocation id;
    private ResourceLocation texture;

    public Ability(Component name, List<Component> description, List<AbilityComponent> components,
                   AttributeList attributes) {
        this.name = name;
        this.rawDescription = description;
        this.description = description.stream().map(line -> (Component) line.copy().withStyle(ChatFormatting.GRAY)).toList();
        this.components = components;
        this.attributes = attributes;
    }

    @Override
    public Component name() {
        return name;
    }

    @Override
    public List<Component> description() {
        return description;
    }

    public List<AbilityComponent> components() {
        return components;
    }

    public AttributeList attributes() {
        return attributes;
    }

    public ResourceLocation getId() {
        if (id == null) {
            id = MythboundCore.ABILITIES.getData().inverse().get(this);
        }
        return id;
    }

    public ResourceLocation getTexture() {
        if (texture == null) {
            getId();
            texture = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/ability/" + id.getPath() + ".png");
        }
        return texture;
    }

    public void tryCast(LivingEntity caster) {
        cast(caster);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new S2CEntityAbilityPacket(caster.getId(), getId()));
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.ability(getId()));
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.ability(getId()));
    }

    public void cast(LivingEntity entity) {
        for (AbilityComponent effect : components) {
            effect.cast(entity);
        }
    }
}
