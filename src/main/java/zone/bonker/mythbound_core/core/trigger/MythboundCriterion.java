package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import zone.bonker.mythbound_core.init.MythboundTriggers;

public record MythboundCriterion<T>(MythboundTrigger<T> trigger, T instance) {
    public static final MapCodec<MythboundCriterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue(
            "trigger", "conditions", MythboundTriggers.CODEC, MythboundCriterion::trigger, MythboundCriterion::criterionCodec);

    private static <T> Codec<MythboundCriterion<T>> criterionCodec(MythboundTrigger<T> trigger) {
        return trigger.codec().xmap(instance -> new MythboundCriterion<>(trigger, (T)instance), MythboundCriterion::instance);
    }
}
