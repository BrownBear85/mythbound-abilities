package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ability.spell.TriggerContext;

import javax.annotation.Nullable;
import java.util.Optional;

public class SpellTrigger extends MythboundTrigger<SpellTrigger.TriggerInstance> {
    public void trigger(LivingEntity caster, ResourceLocation spellId, @Nullable LivingEntity target, int time) {
        TriggerContext context = new TriggerContext(caster);
        context.spellId = spellId;
        context.spellTarget = target;
        trigger(context, instance -> instance.matches(caster, target, time));
    }

    @Override
    public boolean isSpellSpecific() {
        return true;
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> caster, Optional<ContextAwarePredicate> target,
                                  Optional<MinMaxBounds.Ints> time, Optional<Integer> interval) {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("caster").forGetter(TriggerInstance::caster),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("target").forGetter(TriggerInstance::target),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("time").forGetter(TriggerInstance::time),
                Codec.INT.optionalFieldOf("interval").forGetter(TriggerInstance::interval)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(LivingEntity caster, @Nullable LivingEntity target, int time) {
            if (this.caster.isPresent() && !this.caster.get().matches(createContext(caster))) {
                return false;
            }
            if (this.target.isPresent() && (target == null || !this.target.get().matches(createContext(target)))) {
                return false;
            }
            if (this.time.isPresent() && !this.time.get().matches(time)) {
                return false;
            }
            return this.interval.isEmpty() || time % this.interval.get() == 0;
        }
    }
}
