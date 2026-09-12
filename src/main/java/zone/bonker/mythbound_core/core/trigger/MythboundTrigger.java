package zone.bonker.mythbound_core.core.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.*;
import java.util.function.Predicate;

public abstract class MythboundTrigger<T> {
    private final Map<LivingEntity, HashMap<String, Listener<T>>> listenerMap = new IdentityHashMap<>();

    public abstract Codec<T> codec();

    protected void trigger(LivingEntity entity, Predicate<T> instancePredicate) {
        if (listenerMap.containsKey(entity)) {
            for (Listener<T> listener : listenerMap.get(entity).values()) {
                if (instancePredicate.test(listener.instance)) {
                    listener.callback.run();
                }
            }
        }
    }

    public static boolean isIrrelevant(LivingEntity entity) {
        return entity.level().isClientSide() || !CharacterBuild.hasData(entity);
    }

    public static LootContext createContext(LivingEntity entity) {
        LootParams lootParams = new LootParams.Builder((ServerLevel) entity.level())
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    public void addListener(LivingEntity entity, String listenerId, T instance, Runnable callback) {
        listenerMap.computeIfAbsent(entity, e -> new HashMap<>()).put(listenerId, new Listener<>(instance, callback));
    }

    public void removeListener(LivingEntity entity, String listenerId) {
        if (listenerMap.containsKey(entity)) {
            Map<String, Listener<T>> listeners = listenerMap.get(entity);
            listeners.remove(listenerId);
            if (listeners.isEmpty()) {
                listenerMap.remove(entity);
            }
        }
    }

    public void removeAllListeners(LivingEntity entity) {
        listenerMap.remove(entity);
    }

    public record Listener<T>(T instance, Runnable callback) {

    }
}
