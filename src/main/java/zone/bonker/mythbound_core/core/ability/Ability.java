package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AttributeList;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.ability.component.AbilityComponent;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import javax.annotation.Nullable;
import java.util.List;

public final class Ability implements NamedAndDescribed {

    public static final Codec<Ability> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(o -> o.name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(o -> o.rawDescription),
            AbilityType.CODEC.fieldOf("type").forGetter(o -> o.type),
            AbilityTargeting.CODEC.optionalFieldOf("targeting", AbilityTargeting.DEFAULT).forGetter(o -> o.targeting),
            SpellCost.CODEC.optionalFieldOf("cost", SpellCost.NO_COST).forGetter(o -> o.cost),
            AbilityComponent.DIRECT_CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(o -> o.components),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(o -> o.attributes)
    ).apply(inst, Ability::new));

    private final Component name;
    private final List<Component> rawDescription;
    private final List<Component> description;
    private final AbilityType type;
    private final AbilityTargeting targeting;
    private final SpellCost cost;
    private final List<AbilityComponent> components;
    private final AttributeList attributes;
    private ResourceLocation id;
    private ResourceLocation texture;

    public Ability(Component name, List<Component> description, AbilityType type, AbilityTargeting targeting,
                   SpellCost cost, List<AbilityComponent> components, AttributeList attributes) {
        this.name = name;
        this.rawDescription = description;
        this.description = description.stream().map(line -> (Component) line.copy().withStyle(ChatFormatting.GRAY)).toList();
        this.type = type;
        this.targeting = targeting;
        this.cost = cost;
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

    public AbilityType type() {
        return type;
    }

    public AbilityTargeting targeting() {
        return targeting;
    }

    public SpellCost cost() {
        return cost;
    }

    public ResourceLocation getId() {
        if (id == null) {
            id = MythboundCore.ABILITIES.getKeyOrThrow(this);
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

    /**
     * Called whenever when this ability is unlocked or when an entity with this ability is placed into a level.
     */
    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.ability(getId()));

        for (AbilityComponent component : components) {
            component.onInitialize(entity, getId());
        }
    }

    /**
     * Called whenever when this ability is removed or when an entity with this ability leaves a level.
     */
    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.ability(getId()));

        for (AbilityComponent component : components) {
            component.onDeinitialize(entity, getId());
        }
    }

    public void onCast(LivingEntity caster, @Nullable LivingEntity target) {
        for (AbilityComponent component : components) {
            component.onCast(caster, target);
        }
    }

    public void onTick(LivingEntity entity) {
        for (AbilityComponent component : components) {
            component.onTick(entity);
        }
    }
}
