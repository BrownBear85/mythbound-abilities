package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AttributeList;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.ability.component.AbilityComponent;
import zone.bonker.mythbound_core.core.ability.spell.SpellTargeting;
import zone.bonker.mythbound_core.core.ability.spell.SpellBehavior;
import zone.bonker.mythbound_core.core.ability.spell.SpellCost;
import zone.bonker.mythbound_core.data.MythboundSerialization;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import javax.annotation.Nullable;
import java.util.List;

public final class Ability implements NamedAndDescribed {

    public static final Codec<Ability> CODEC = RecordCodecBuilder.<Ability>create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(o -> o.name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(o -> o.rawDescription),
            AbilityType.CODEC.fieldOf("type").forGetter(o -> o.type),
            SpellBehavior.CODEC.optionalFieldOf("spell_behavior", SpellBehavior.DEFAULT).forGetter(o -> o.spellBehavior),
            AbilityComponent.DIRECT_CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(o -> o.components),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(o -> o.attributes)
    ).apply(inst, Ability::new)).validate(Ability::validate);

    private final Component name;
    private final List<Component> rawDescription;
    private final List<Component> description;
    private final AbilityType type;
    private final SpellBehavior spellBehavior;
    private final List<AbilityComponent> components;
    private final AttributeList attributes;
    private ResourceLocation id;
    private ResourceLocation texture;

    public Ability(Component name, List<Component> description, AbilityType type, SpellBehavior spellBehavior,
                   List<AbilityComponent> components, AttributeList attributes) {
        this.name = name;
        this.rawDescription = description;
        this.description = description.stream().map(line -> (Component) line.copy().withStyle(ChatFormatting.GRAY)).toList();
        this.type = type;
        this.spellBehavior = spellBehavior;
        this.components = components;
        this.attributes = attributes;
    }

    private DataResult<Ability> validate() {
        if (!type.isCastable() && spellBehavior != SpellBehavior.DEFAULT) {
            return DataResult.error(() -> "Non-castable abilities cannot define spell behavior", this);
        }
        return DataResult.success(this);
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

    public SpellBehavior spellBehavior() {
        return spellBehavior;
    }

    public SpellTargeting targeting() {
        return spellBehavior.targets();
    }

    public SpellCost cost() {
        return spellBehavior.cost();
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

        for (int i = 0; i < components.size(); i++) {
            components.get(i).onInitialize(entity, getId(), i);
        }
    }

    /**
     * Called whenever when this ability is removed or when an entity with this ability leaves a level.
     */
    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.ability(getId()));

        for (int i = 0; i < components.size(); i++) {
            components.get(i).onDeinitialize(entity, getId(), i);
        }
    }
}
