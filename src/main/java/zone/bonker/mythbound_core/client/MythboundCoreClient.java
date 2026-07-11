package zone.bonker.mythbound_core.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.server.MythboundRegistryArgument;

import java.util.Objects;

@Mod(value = MythboundCore.MODID, dist = Dist.CLIENT)
public class MythboundCoreClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final int SUCCESS = 1, FAILURE = 0;

    public MythboundCoreClient(IEventBus modEventBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    //// EVENTS

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
}
