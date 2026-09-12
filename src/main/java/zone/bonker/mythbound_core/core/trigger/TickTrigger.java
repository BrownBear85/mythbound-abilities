package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import java.util.Optional;

@EventBusSubscriber
public class TickTrigger extends MythboundTrigger<TickTrigger.TriggerInstance> {
    public TickTrigger() {
        System.out.println("constructed " + this);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || !entity.isAlive() || isIrrelevant(entity)) {
            return;
        }

        MythboundTriggers.TICK.get().trigger(entity, instance -> instance.matches(entity));
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> entity, int interval) {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("attacker").forGetter(TriggerInstance::entity),
                Codec.INT.optionalFieldOf("interval", 1).forGetter(TriggerInstance::interval)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(LivingEntity entity) {
            return entity.tickCount % interval == 0;
        }
    }
}
