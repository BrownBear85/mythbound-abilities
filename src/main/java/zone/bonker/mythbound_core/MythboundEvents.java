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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AttributeList;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.core.ability_effect.MythboundEffect;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

@EventBusSubscriber
public class MythboundEvents {

    //// ENTITIES

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
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

            for (Iterator<ResourceLocation> iterator = data.getUnlockedAbilityIds().iterator(); iterator.hasNext(); ) {
                ResourceLocation id = iterator.next();
                Ability ability = MythboundCore.ABILITIES.getData().get(id);
                if (ability == null) {
                    MythboundCore.LOGGER.warn("Tried to apply attributes for unregistered ability {}, removing from entity {}", id, entity);
                    iterator.remove();
                } else {
                    ability.attributes().apply(entity, AttributeList.ability(id));
                }
            }
        });
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        CharacterBuild.getExisting(entity).ifPresent(data -> {
            Set<MythboundEffect> effects = new HashSet<>();

            Race race = data.getRace();
            if (race != null) {
                effects.addAll(race.effects());
            }

            CharacterClass characterClass = data.getCharacterClass();
            if (characterClass != null) {
                effects.addAll(characterClass.effects());
            }

            for (Iterator<ResourceLocation> iterator = data.getUnlockedAbilityIds().iterator(); iterator.hasNext(); ) {
                ResourceLocation id = iterator.next();
                Ability ability = MythboundCore.ABILITIES.getData().get(id);
                if (ability == null) {
                    MythboundCore.LOGGER.warn("Tried to tick unregistered ability {}, removing from entity {}", id, entity);
                    iterator.remove();
                } else {
                    effects.addAll(ability.effects());
                }
            }

            effects.forEach(effect -> effect.tick(entity));
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
