package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.List;

public record Subclass(Component name, List<Component> description, AbilityTree abilityTree)
        implements NamedAndDescribed {

    public static final Codec<Subclass> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(Subclass::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(Subclass::description),
            AbilityTree.CODEC.fieldOf("ability_tree").forGetter(Subclass::abilityTree)
    ).apply(inst, Subclass::new));
}
