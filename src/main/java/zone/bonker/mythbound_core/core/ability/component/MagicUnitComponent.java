package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ability.spell.TriggerContext;
import zone.bonker.mythbound_core.core.trigger.MythboundCriterion;
import zone.bonker.mythbound_core.data.SpellData;

import java.util.Optional;
import java.util.function.BiFunction;

public record MagicUnitComponent(Optional<MythboundCriterion<?>> criterion, ResourceLocation unit, Operation operation,
                                 double amount) implements AbilityComponent {
    public static final MapCodec<MagicUnitComponent> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            AbilityComponent.criterionCodec(),
            ResourceLocation.CODEC.fieldOf("unit").forGetter(MagicUnitComponent::unit),
            Operation.CODEC.fieldOf("operation").forGetter(MagicUnitComponent::operation),
            Codec.DOUBLE.fieldOf("amount").forGetter(MagicUnitComponent::amount)
    ).apply(inst, MagicUnitComponent::new));

    @Override
    public MapCodec<? extends AbilityComponent> codec() {
        return CODEC;
    }

    @Override
    public void onInitialize(LivingEntity entity, ResourceLocation abilityId, int index) {
        SpellData.get(entity).registerAssociation(unit, abilityId);
    }

    @Override
    public void onTrigger(TriggerContext context) {
        if (context.level.isClientSide) {
            return;
        }

        SpellData spellData = SpellData.get(context.caster);
        spellData.setUnitAmount(unit, operation.apply(spellData.getUnitAmount(unit), amount));
    }

    public enum Operation implements StringRepresentable {
        ADD("add", (x, y) -> x + y),
        SUBTRACT("subtract", (x, y) -> x - y),
        MULTIPLY("multiply", (x, y) -> x * y),
        DIVIDE("divide", (x, y) -> x / y),
        SET("set", (x, y) -> y);

        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final String name;
        private final BiFunction<Integer, Double, Double> function;

        Operation(String name, BiFunction<Integer, Double, Double> function) {
            this.name = name;
            this.function = function;
        }

        public Integer apply(Integer units, Double amount) {
            return Mth.floor(function.apply(units, amount));
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
