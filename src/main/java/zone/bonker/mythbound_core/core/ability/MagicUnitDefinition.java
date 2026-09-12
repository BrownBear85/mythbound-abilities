package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.trigger.MythboundCriterion;
import zone.bonker.mythbound_core.data.MythboundSerialization;
import zone.bonker.mythbound_core.data.SpellData;

import java.util.List;

public record MagicUnitDefinition(Component name, int color, int maximum, DisplayType display, List<ListenerDefinition<?>> defaultListeners) {
    public static final MapCodec<MagicUnitDefinition> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(o -> o.name),
            MythboundSerialization.COLOR_CODEC.fieldOf("color").forGetter(o -> o.color),
            Codec.INT.fieldOf("maximum").forGetter(o -> o.maximum),
            StringRepresentable.fromEnum(DisplayType::values).fieldOf("display").forGetter(o -> o.display),
            ListenerDefinition.CODEC.listOf().optionalFieldOf("listeners", List.of()).forGetter(o -> o.defaultListeners)
    ).apply(inst, MagicUnitDefinition::new));

    public static final Codec<MagicUnitDefinition> CODEC = MAP_CODEC.codec();

    public enum DisplayType implements StringRepresentable {
        NONE("none"), ABILITY_SPECIFIC("ability_adjacent"), GLOBAL("global");

        private final String name;

        DisplayType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record ListenerDefinition<T>(MythboundCriterion<T> criterion, int amount, float chance) {
        public static final Codec<ListenerDefinition<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                MythboundCriterion.MAP_CODEC.forGetter(ListenerDefinition::criterion),
                Codec.INT.fieldOf("amount").forGetter(ListenerDefinition::amount),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(ListenerDefinition::chance)
        ).apply(inst, ListenerDefinition::new));

        public void addListener(LivingEntity entity, ResourceLocation unitId, String source, int index) {
            criterion.trigger().addListener(entity, makeListenerId(unitId, source, index), criterion.instance(), () -> {
                if (entity.getRandom().nextFloat() < chance) {
                    SpellData.get(entity).addUnits(unitId, amount);
                }
            });
        }

        public void removeListener(LivingEntity entity, ResourceLocation unitId, String source, int index) {
            criterion.trigger().removeListener(entity, makeListenerId(unitId, source, index));
        }

        private String makeListenerId(ResourceLocation unitId, String source, int index) {
            return unitId.toString() + "-" + source + "-" + index;
        }
    }
}
