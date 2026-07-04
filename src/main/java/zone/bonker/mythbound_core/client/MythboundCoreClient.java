package zone.bonker.mythbound_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.context.CommandContext;
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
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AbilityBinding;
import zone.bonker.mythbound_core.server.AbilityArgument;

import java.util.Objects;

@Mod(value = MythboundCore.MODID, dist = Dist.CLIENT)
public class MythboundCoreClient {
    public MythboundCoreClient(IEventBus modEventBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
    }

    //// EVENTS

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ability")
                .then(Commands.literal("bind")
                        .then(Commands.argument("ability", AbilityArgument.ability())
                                .suggests(AbilityArgument.SUGGEST_UNLOCKED)
                                .executes(MythboundCoreClient::setBind))));
    }

    //// COMMANDS

    private static int setBind(CommandContext<CommandSourceStack> context) {
        Ability ability = context.getArgument("ability", Ability.class);
        AbilityInputHandler.abilityToBind = ability.getId();
        context.getSource().sendSystemMessage(
                Component.translatable("commands." + MythboundCore.MODID + ".binding", ability.getName()));
        return 1;
    }

    //// METHODS

    public static boolean onMac() {
        return Minecraft.ON_OSX;
    }

    public static Component getDisplayName(AbilityBinding binding) {
        Component component = InputConstants.Type.values()[binding.type()].getOrCreate(binding.key()).getDisplayName();
        if (binding.alt()) {
            component = Component.translatable("neoforge.controlsgui.alt", component);
        }
        if (binding.control()) {
            component = Component.translatable(MythboundCoreClient.onMac() ? "neoforge.controlsgui.control.mac" : "neoforge.controlsgui.control", component);
        }
        if (binding.shift()) {
            component = Component.translatable("neoforge.controlsgui.shift", component);
        }
        return component;
    }

    public static RegistryAccess getRegistryLookup() {
        return Objects.requireNonNull(Minecraft.getInstance().getConnection()).registryAccess();
    }
}
