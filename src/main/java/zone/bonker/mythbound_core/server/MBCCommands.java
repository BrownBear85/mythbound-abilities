package zone.bonker.mythbound_core.server;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.data.EntityAbilities;

@EventBusSubscriber
public class MBCCommands {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_INFOS =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MythboundCore.MODID);

    static {
        ArgumentTypeInfos.registerByClass(AbilityArgument.class, AbilityArgument.INFO);
        ARGUMENT_TYPE_INFOS.register("ability_argument", () -> AbilityArgument.INFO);

//        SuggestionProviders.<CommandSourceStack>register(MythboundCore.identifier("suggest_all_abilities"), AbilityArgument.SUGGEST_ALL);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ability")
                .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("ability", AbilityArgument.ability())
                                .suggests(AbilityArgument.SUGGEST_ALL)
                                .executes(MBCCommands::unlock)))
                .then(Commands.literal("info")
                        .then(Commands.argument("ability", AbilityArgument.ability())
                                .suggests(AbilityArgument.SUGGEST_ALL)
                                .executes(MBCCommands::info))));
    }

    private static int unlock(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof LivingEntity livingEntity)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".invalid_target"));
            return 0;
        }

        Ability ability = context.getArgument("ability", Ability.class);
        if (EntityAbilities.unlockAbility(livingEntity, ability)) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".unlocked_ability", ability.getName()), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".already_unlocked"));
            return 0;
        }
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        Ability ability = context.getArgument("ability", Ability.class);
        context.getSource().sendSystemMessage(ability.getName());
        for (Component line : ability.getDescription()) {
            context.getSource().sendSystemMessage(Component.literal("  ").append(line));
        }
        return 1;
    }
}
