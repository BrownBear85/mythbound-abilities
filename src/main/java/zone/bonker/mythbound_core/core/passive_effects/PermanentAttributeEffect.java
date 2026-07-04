package zone.bonker.mythbound_core.core.passive_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AbilityEffect;

import java.util.List;

public class PermanentAttributeEffect extends AbilityEffect {
    public static final MapCodec<PermanentAttributeEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Entry.CODEC.listOf().fieldOf("modifiers").forGetter(o -> o.modifiers)
    ).apply(inst, PermanentAttributeEffect::new));

    private final List<Entry> modifiers;

    public PermanentAttributeEffect(List<Entry> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public MapCodec<? extends AbilityEffect> codec() {
        return CODEC;
    }

    @Override
    public void onUnlock(Level level, LivingEntity entity) {
        for (int i = 0; i < modifiers.size(); i++) {
            Entry entry = modifiers.get(i);
            AttributeInstance instance = entity.getAttribute(entry.attribute);
            if (instance != null) {
                instance.addPermanentModifier(new AttributeModifier(MythboundCore.identifier("modifier" + i), entry.amount, entry.operation));
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
