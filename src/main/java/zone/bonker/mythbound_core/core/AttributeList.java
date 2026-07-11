package zone.bonker.mythbound_core.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import zone.bonker.mythbound_core.MythboundCore;

import java.util.List;

public record AttributeList(List<Entry> entries) {
    public static final AttributeList EMPTY = new AttributeList(List.of());

    public static final Codec<AttributeList> CODEC =
            Entry.CODEC.listOf().xmap(AttributeList::new, AttributeList::entries);

    public static final ResourceLocation RACE = MythboundCore.identifier("race");
    public static final ResourceLocation CLASS = MythboundCore.identifier("class");

    private Multimap<Holder<Attribute>, AttributeModifier> createModifierMap(ResourceLocation sourceId) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            map.put(entry.attribute, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(sourceId.getNamespace(), sourceId.getPath() + "/modifier_" + i),
                    entry.amount,
                    entry.operation));
        }
        return map;
    }

    public static ResourceLocation ability(ResourceLocation abilityId) {
        return ResourceLocation.fromNamespaceAndPath(abilityId.getNamespace(), "ability/" + abilityId.getPath());
    }

    public void apply(LivingEntity entity, ResourceLocation sourceId) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = createModifierMap(sourceId);

        for (Holder<Attribute> attribute : modifiers.keySet()) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }

            for (AttributeModifier modifier : modifiers.get(attribute)) {
                instance.addOrUpdateTransientModifier(modifier);
            }
        }
    }

    public void remove(LivingEntity entity, ResourceLocation sourceId) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = createModifierMap(sourceId);

        for (Holder<Attribute> attribute : modifiers.keySet()) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }

            for (AttributeModifier modifier : modifiers.get(attribute)) {
                instance.removeModifier(modifier);
            }
        }
    }

    public record Entry(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Attribute.CODEC.fieldOf("attribute").forGetter(Entry::attribute),
                Codec.DOUBLE.fieldOf("amount").forGetter(Entry::amount),
                AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Entry::operation)
        ).apply(inst, Entry::new));
    }
}
