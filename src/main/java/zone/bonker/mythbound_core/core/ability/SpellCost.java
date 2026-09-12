package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.data.SpellData;

import java.util.Collection;
import java.util.List;

public record SpellCost(List<Entry> entries) {
    public static final Codec<SpellCost> CODEC = Entry.CODEC.listOf().xmap(SpellCost::new, SpellCost::entries);

    public static final SpellCost NO_COST = new SpellCost(List.of());

    public boolean canCast(LivingEntity entity) {
        SpellData spellData = SpellData.get(entity);
        for (Entry entry : entries) {
            if (spellData.getUnitAmount(entry.unit) < entry.amount) {
                return false;
            }
        }
        return true;
    }

    public void consumeUnits(LivingEntity entity) {
        SpellData spellData = SpellData.get(entity);
        for (Entry entry : entries) {
            spellData.addUnits(entry.unit, -entry.amount);
        }
    }

    public Collection<ResourceLocation> getRelevantUnits() {
        return entries.stream().map(Entry::unit).toList();
    }

    public record Entry(ResourceLocation unit, int amount) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("unit").forGetter(Entry::unit),
                Codec.INT.fieldOf("amount").forGetter(Entry::amount)
        ).apply(inst, Entry::new));
    }
}
