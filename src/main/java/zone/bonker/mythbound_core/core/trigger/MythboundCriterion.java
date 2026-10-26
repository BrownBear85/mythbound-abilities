package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ability.spell.TriggerContext;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import java.util.function.Consumer;

public record MythboundCriterion<T>(MythboundTrigger<T> trigger, T instance) {
    public static final MapCodec<MythboundCriterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue(
            "trigger", "conditions", MythboundTriggers.CODEC, MythboundCriterion::trigger, MythboundCriterion::criterionCodec);

    private static <T> Codec<MythboundCriterion<T>> criterionCodec(MythboundTrigger<T> trigger) {
        return trigger.codec().xmap(instance -> new MythboundCriterion<>(trigger, instance), MythboundCriterion::instance);
    }

    public void addListener(LivingEntity entity, String listenerId, Consumer<TriggerContext> callback) {
        trigger.addListener(entity, listenerId, instance, callback);
    }

    public void removeListener(LivingEntity entity, String listenerId) {
        trigger.removeListener(entity, listenerId);
    }
}
