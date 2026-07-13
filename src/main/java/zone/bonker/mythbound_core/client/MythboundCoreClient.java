package zone.bonker.mythbound_core.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import zone.bonker.mythbound_core.CharacterModelExtensions;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.ModelProperties;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.server.MythboundRegistryArgument;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mod(value = MythboundCore.MODID, dist = Dist.CLIENT)
public class MythboundCoreClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final int SUCCESS = 1, FAILURE = 0;

    // Race/Class model properties
    private static final Map<ModelProperties, PlayerRenderer> WIDE_RENDERER_MAP = new HashMap<>();
    private static final Map<ModelProperties, PlayerRenderer> SLIM_RENDERER_MAP = new HashMap<>();
    private static EntityRendererProvider.Context CAPTURED_CONTEXT;
    @Nullable
    public static ModelProperties CURRENT_MODEL_PROPERTIES = null;

    public MythboundCoreClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addEntityLayers);

        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    //// EVENTS

    private void clientSetup(FMLClientSetupEvent event) {
        CharacterModelExtensions.reload();
    }

    private void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        WIDE_RENDERER_MAP.clear();
        SLIM_RENDERER_MAP.clear();
        CharacterModelExtensions.reload();
        CAPTURED_CONTEXT = event.getContext();
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mythbound")
                .then(Commands.literal("bind")
                        .then(Commands.argument("ability", MythboundRegistryArgument.ability())
                                .suggests(MythboundRegistryArgument.SUGGEST_UNLOCKED_ABILITIES)
                                .executes(MythboundCoreClient::startBinding))));
    }

    //// COMMANDS

    private static int startBinding(CommandContext<CommandSourceStack> context) {
        if (Minecraft.getInstance().player == null) {
            return FAILURE;
        }

        Ability ability = context.getArgument("ability", Ability.class);

        if (!CharacterBuild.get(Minecraft.getInstance().player).hasAbility(ability.getId())) {
            context.getSource().sendFailure(
                    Component.translatable("commands." + MythboundCore.MODID + ".ability_not_unlocked"));
            return FAILURE;
        }

        AbilityInputHandler.abilityToBind = ability.getId();
        context.getSource().sendSystemMessage(
                Component.translatable("commands." + MythboundCore.MODID + ".binding", ability.name()));
        return SUCCESS;
    }

    //// METHODS

    public static boolean onMac() {
        return Minecraft.ON_OSX;
    }

    public static RegistryAccess getRegistryLookup() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityRenderer<T> replaceEntityRenderer(T entity, EntityRenderer<T> original) {
        if (!(entity instanceof AbstractClientPlayer player)) {
            return original;
        }

        Optional<CharacterBuild> optional = CharacterBuild.getExisting(player);
        if (optional.isEmpty()) {
            return original;
        }

        Race race = optional.get().getRace();
        if (race == null || (!race.modelProperties().hasCustomProportions() && !CharacterModelExtensions.hasExtraLayers(race))) {
            return original;
        }

        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        Map<ModelProperties, PlayerRenderer> map = slim ? WIDE_RENDERER_MAP : SLIM_RENDERER_MAP;

        if (!map.containsKey(race.modelProperties())) {
            CURRENT_MODEL_PROPERTIES = race.modelProperties();

            PlayerRenderer renderer = new PlayerRenderer(CAPTURED_CONTEXT, slim);

            CharacterModelExtensions.addExtraLayers(race, renderer, CAPTURED_CONTEXT.getModelSet());
            CharacterClass characterClass = optional.get().getCharacterClass();
            if (characterClass != null) {
                CharacterModelExtensions.addExtraLayers(characterClass, renderer, CAPTURED_CONTEXT.getModelSet());
            }

            map.put(race.modelProperties(), renderer);

            CURRENT_MODEL_PROPERTIES = null;
        }

        return (EntityRenderer<T>) map.get(race.modelProperties());
    }
}
