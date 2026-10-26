package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import zone.bonker.mythbound_core.core.ability.spell.TriggerContext;
import zone.bonker.mythbound_core.core.trigger.MythboundCriterion;

import java.util.Optional;

public record TestMessageComponent(Optional<MythboundCriterion<?>> criterion, String message) implements AbilityComponent {
    public static final MapCodec<TestMessageComponent> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            AbilityComponent.criterionCodec(),
            Codec.STRING.fieldOf("message").forGetter(o -> o.message)
    ).apply(inst, TestMessageComponent::new));

    @Override
    public MapCodec<? extends AbilityComponent> codec() {
        return CODEC;
    }

    @Override
    public void onTrigger(TriggerContext context) {
        if (context.level.isClientSide()) {
            return;
        }

        context.caster.sendSystemMessage(Component.literal(message));
    }
}
