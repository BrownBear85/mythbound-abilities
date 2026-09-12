package zone.bonker.mythbound_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
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
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.gui.overlay.AbilityOverlay;
import zone.bonker.mythbound_core.client.gui.overlay.TargetingOverlay;
import zone.bonker.mythbound_core.client.gui.overlay.UnitsOverlay;
import zone.bonker.mythbound_core.client.gui.screen.ability_tree.AbilityTreesScreen;
import zone.bonker.mythbound_core.client.gui.screen.ability_tree.RefreshWithCharacterBuild;
import zone.bonker.mythbound_core.client.model.CharacterModelExtensions;
import zone.bonker.mythbound_core.core.ability.Ability;
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

    // Keybinds
    public static final KeyMapping TARGETING_KEYBIND = new KeyMapping("key." + MythboundCore.MODID + ".target",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            MythboundCore.MODID);

    public MythboundCoreClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addEntityLayers);
        modEventBus.addListener(this::registerGuiLayers);
        modEventBus.addListener(this::registerKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(this::keyInput);
    }

    //// EVENTS

    private void clientSetup(FMLClientSetupEvent event) {
        CharacterModelExtensions.reload(); // TODO: check if reloading here is necessary
    }

    private void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        WIDE_RENDERER_MAP.clear();
        SLIM_RENDERER_MAP.clear();
        CharacterModelExtensions.reload();
        CAPTURED_CONTEXT = event.getContext();
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(TargetingOverlay.ID, new TargetingOverlay());
        event.registerAbove(TargetingOverlay.ID, AbilityOverlay.ID, new AbilityOverlay());
        event.registerAbove(AbilityOverlay.ID, UnitsOverlay.ID, new UnitsOverlay());
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TARGETING_KEYBIND);
    }

    private void keyInput(InputEvent.Key event) {
        while (TARGETING_KEYBIND.consumeClick()) {
            ClientSpellTargeting.pressedKeybind();
        }
    }

    //// COMMANDS

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mythbound")
                .then(Commands.literal("bind")
                        .then(Commands.argument("ability", MythboundRegistryArgument.ability())
                                .suggests(MythboundRegistryArgument.SUGGEST_UNLOCKED_ABILITIES)
                                .executes(MythboundCoreClient::startBinding)))
                .then(Commands.literal("ability_tree")
                        .executes(context -> {
                            if (Minecraft.getInstance().player != null && CharacterBuild.get(Minecraft.getInstance().player).getCharacterClass() != null) {
                                Minecraft.getInstance().setScreen(new AbilityTreesScreen(CharacterBuild.get(Minecraft.getInstance().player)));
                            }
                            return 1;
                        })));
    }

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

    public static void refreshCurrentScreen(CharacterBuild characterBuild) {
        if (Minecraft.getInstance().screen instanceof RefreshWithCharacterBuild refreshScreen) {
            refreshScreen.refreshWidgets(characterBuild);
        }
    }
}
