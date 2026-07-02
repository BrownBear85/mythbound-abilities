package zone.bonker.mb_abilities.server;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.data.EntityAbilities;
import zone.bonker.mb_abilities.init.Abilities;

@EventBusSubscriber
public class AbilityCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ability")
                .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("set")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, MythboundAbilities.ABILITY_COUNT))
                                .then(Commands.argument("ability", ResourceArgument.resource(event.getBuildContext(), Abilities.ABILITIES_KEY))
                                        .executes(AbilityCommands::setSlot))))
                .then(Commands.literal("get")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(1, MythboundAbilities.ABILITY_COUNT))
                                .executes(AbilityCommands::getSlot)))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("ability", ResourceArgument.resource(event.getBuildContext(), Abilities.ABILITIES_KEY))
                                .executes(AbilityCommands::unlock)))
                .then(Commands.literal("info")
                        .then(Commands.argument("ability", ResourceArgument.resource(event.getBuildContext(), Abilities.ABILITIES_KEY))
                                .executes(AbilityCommands::info))));
    }

    private static int setSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof LivingEntity livingEntity)) {
            context.getSource().sendFailure(Component.translatable("commands.mb_abilities.invalid_target"));
            return 0;
        }

        Holder.Reference<Ability> holder = ResourceArgument.getResource(context, "ability", Abilities.ABILITIES_KEY);
        if (!EntityAbilities.isUnlocked(livingEntity, holder.key().location())) {
            context.getSource().sendFailure(Component.translatable("commands.mb_abilities.ability_not_unlocked"));
            return 0;
        }

        int slot = context.getArgument("slot", int.class);

        if (EntityAbilities.setAbilityInSlot(livingEntity, slot, holder.key().location())) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.mb_abilities.set_ability_in_slot", holder.value().getName(context.getSource().registryAccess()), slot), false);
            return 1;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.mb_abilities.ability_already_in_slot"), false);
            return 0;
        }
    }

    private static int getSlot(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof LivingEntity livingEntity)) {
            context.getSource().sendFailure(Component.translatable("commands.mb_abilities.invalid_target"));
            return 0;
        }

        int slot = context.getArgument("slot", int.class);
        Ability ability = EntityAbilities.getAbilityInSlot(livingEntity, slot);

        if (ability == null) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.mb_abilities.empty_slot", slot), false);
            return 0;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.mb_abilities.get_ability_in_slot", ability.getName(context.getSource().registryAccess()), slot), false);
            return 1;
        }
    }

    private static int unlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof LivingEntity livingEntity)) {
            context.getSource().sendFailure(Component.translatable("commands.mb_abilities.invalid_target"));
            return 0;
        }

        Holder.Reference<Ability> holder = ResourceArgument.getResource(context, "ability", Abilities.ABILITIES_KEY);

        if (EntityAbilities.unlockAbility(livingEntity, holder.key().location())) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.mb_abilities.unlocked_ability", holder.value().getName(context.getSource().registryAccess())), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("commands.mb_abilities.already_unlocked"));
            return 0;
        }
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder.Reference<Ability> holder = ResourceArgument.getResource(context, "ability", Abilities.ABILITIES_KEY);
        context.getSource().sendSystemMessage(holder.value().getName(context.getSource().registryAccess()));
        for (Component line : holder.value().getDescription(context.getSource().registryAccess())) {
            context.getSource().sendSystemMessage(Component.literal("  ").append(line));
        }
        return 1;
    }
}
