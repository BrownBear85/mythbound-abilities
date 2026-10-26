package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ability.spell.TriggerContext;
import zone.bonker.mythbound_core.core.trigger.MythboundCriterion;
import zone.bonker.mythbound_core.init.AbilityComponentSerializers;

import java.util.Optional;
import java.util.function.Function;

public interface AbilityComponent {
    Codec<AbilityComponent> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilityComponentSerializers.REGISTRY.byNameCodec().dispatch(AbilityComponent::codec, Function.identity()));

    static <T extends AbilityComponent> RecordCodecBuilder<T, Optional<MythboundCriterion<?>>> criterionCodec() {
        return MythboundCriterion.MAP_CODEC.<Optional<MythboundCriterion<?>>>xmap(Optional::ofNullable, o -> o.orElse(null)).forGetter(AbilityComponent::criterion);
    }

    Optional<MythboundCriterion<?>> criterion();

    MapCodec<? extends AbilityComponent> codec();

    default void onInitialize(LivingEntity entity, ResourceLocation abilityId, int index) {
        criterion().ifPresent(criterion -> criterion.addListener(entity, makeListenerId(abilityId, index), context -> {
            if (!criterion.trigger().isSpellSpecific() || abilityId.equals(context.spellId)) {
                onTrigger(context);
            }
        }));
    }

    default void onDeinitialize(LivingEntity entity, ResourceLocation abilityId, int index) {
        criterion().ifPresent(criterion -> criterion.removeListener(entity, makeListenerId(abilityId, index)));
    }

    default void onTrigger(TriggerContext context) {

    }

    static String makeListenerId(ResourceLocation abilityId, int index) {
        return "ability-component-" + abilityId + "-" + index;
    }
}
