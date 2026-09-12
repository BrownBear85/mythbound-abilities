package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import java.util.Optional;

@EventBusSubscriber
public class KillEntityTrigger extends MythboundTrigger<KillEntityTrigger.TriggerInstance> {
    public KillEntityTrigger() {
        System.out.println("constructed " + this);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().isAlive()) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        boolean directDeath = true;
        if (attacker == null) {
            attacker = event.getEntity().getKillCredit();
            directDeath = false;
        }

        if (!(attacker instanceof LivingEntity livingAttacker) || isIrrelevant(livingAttacker)) {
            return;
        }

        final boolean finalDirectDeath = directDeath;
        MythboundTriggers.KILL_ENTITY.get().trigger(livingAttacker, instance -> instance.matches(livingAttacker, event.getEntity(), event.getSource(), finalDirectDeath));
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> attacker, Optional<ContextAwarePredicate> victim,
                                  Optional<DamageSourcePredicate> killingBlow, boolean allowIndirectDeath) {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("attacker").forGetter(TriggerInstance::attacker),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("victim").forGetter(TriggerInstance::victim),
                DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(TriggerInstance::killingBlow),
                Codec.BOOL.optionalFieldOf("allow_indirect_death", false).forGetter(TriggerInstance::allowIndirectDeath)
        ).apply(inst, TriggerInstance::new));

        public boolean matches(LivingEntity attacker, LivingEntity victim, DamageSource damageSource, boolean directDeath) {
            if (!directDeath && !allowIndirectDeath) {
                return false;
            }
            if (this.attacker.isPresent() && !this.attacker.get().matches(createContext(attacker))) {
                return false;
            }
            if (this.victim.isPresent() && !this.victim.get().matches(createContext(victim))) {
                return false;
            }
            return this.killingBlow.isEmpty() || this.killingBlow.get().matches((ServerLevel) attacker.level(), attacker.position(), damageSource);
        }
    }
}
