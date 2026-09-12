package zone.bonker.mythbound_core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.core.ability.MagicUnitDefinition;
import zone.bonker.mythbound_core.core.trigger.MythboundTrigger;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import java.util.Map;
import java.util.Optional;

@EventBusSubscriber
public class MythboundEvents {

    //// ENTITIES

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        for (Map.Entry<ResourceLocation, MagicUnitDefinition> entry : MythboundCore.MAGIC_UNITS.getData().entrySet()) {
            int i = 0;
            for (MagicUnitDefinition.ListenerDefinition<?> listenerDefinition : entry.getValue().defaultListeners()) {
                listenerDefinition.addListener(entity, entry.getKey(), "default", i++);
            }
        }

        CharacterBuild.getExisting(entity).ifPresent(data -> {
            Race race = data.getRace();
            if (race != null) {
                race.initialize(entity);
            }

            CharacterClass characterClass = data.getCharacterClass();
            if (characterClass != null) {
                characterClass.initialize(entity);
            }

            for (ResourceLocation id : data.getAbilities()) {
                MythboundCore.ABILITIES.getOrThrow(id).initialize(entity);
            }
        });
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        for (MythboundTrigger<?> trigger : MythboundTriggers.REGISTRY) {
            trigger.removeAllListeners(entity);
        }

        CharacterBuild.getExisting(entity).ifPresent(data -> {
            Race race = data.getRace();
            if (race != null) {
                race.deinitialize(entity);
            }

            CharacterClass characterClass = data.getCharacterClass();
            if (characterClass != null) {
                characterClass.deinitialize(entity);
            }

            for (ResourceLocation id : data.getAbilities()) {
                MythboundCore.ABILITIES.getOrThrow(id).deinitialize(entity);
            }
        });
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        CharacterBuild.getExisting(entity).ifPresent(data -> {
            for (ResourceLocation id : data.getAbilities()) {
                MythboundCore.ABILITIES.getOrThrow(id).onTick(entity);
            }
        });
    }

    //// PLAYERS

    @SubscribeEvent
    public static void playerLoggingIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            MythboundCore.REGISTRIES.syncToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void modifyPlayerDimensions(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Optional<CharacterBuild> optional = CharacterBuild.getExisting(event.getEntity());
        if (optional.isEmpty()) {
            return;
        }

        Race race = optional.get().getRace();
        if (race == null || !race.modelProperties().hasCustomHitbox()) {
            return;
        }

        event.setNewSize(event.getNewSize().scale(
                race.modelProperties().hitboxScaleX(),
                race.modelProperties().hitboxScaleY()
        ));
    }
}
