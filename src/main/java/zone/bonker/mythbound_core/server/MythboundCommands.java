package zone.bonker.mythbound_core.server;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.List;

@EventBusSubscriber
public class MythboundCommands {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPE_INFOS =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MythboundCore.MODID);

    private static final int SUCCESS = 1, FAILURE = 0;

    private static final SimpleCommandExceptionType ERROR_NOT_LIVING = new SimpleCommandExceptionType(
            Component.translatableEscape("commands." + MythboundCore.MODID + ".not_living"));

    static {
        ArgumentTypeInfos.registerByClass(fixClassType(MythboundRegistryArgument.class), MythboundRegistryArgument.INFO);
        ARGUMENT_TYPE_INFOS.register("ability_argument", () -> MythboundRegistryArgument.INFO);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mythbound")
                .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("get")
                        .then(Commands.literal("race")
                                .executes(MythboundCommands::getRace))
                        .then(Commands.literal("class")
                                .executes(MythboundCommands::getClass))
                        .then(Commands.literal("abilities")
                                .executes(MythboundCommands::getAbilities)))
                .then(Commands.literal("set")
                        .then(Commands.literal("race")
                                .then(Commands.argument("race", MythboundRegistryArgument.race())
                                        .suggests(MythboundRegistryArgument.SUGGEST_ALL_RACES)
                                        .executes(MythboundCommands::setRace)))
                        .then(Commands.literal("class")
                                .then(Commands.argument("class", MythboundRegistryArgument.characterClass())
                                        .suggests(MythboundRegistryArgument.SUGGEST_ALL_CLASSES)
                                        .executes(MythboundCommands::setClass))))
                .then(Commands.literal("info")
                        .then(Commands.literal("race")
                                .then(Commands.argument("race", MythboundRegistryArgument.race())
                                        .suggests(MythboundRegistryArgument.SUGGEST_ALL_RACES)
                                        .executes(context -> sendInfo(context, context.getArgument("race", Race.class)))))
                        .then(Commands.literal("class")
                                .then(Commands.argument("class", MythboundRegistryArgument.characterClass())
                                        .suggests(MythboundRegistryArgument.SUGGEST_ALL_CLASSES)
                                        .executes(context -> sendInfo(context, context.getArgument("class", CharacterClass.class)))))
                        .then(Commands.literal("ability")
                                .then(Commands.argument("ability", MythboundRegistryArgument.ability())
                                        .suggests(MythboundRegistryArgument.SUGGEST_ALL_ABILITIES)
                                        .executes(context -> sendInfo(context, context.getArgument("ability", Ability.class))))))
                .then(Commands.literal("reload")
                        .executes(MythboundCommands::reloadBuild)));
    }

    private static int getRace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Race race = CharacterBuild.get(verifyLivingEntity(context)).getRace();
        if (race == null) {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".no_race"));
        } else {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".get_race", race.name()));
        }
        return SUCCESS;
    }

    private static int getClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CharacterClass characterClass = CharacterBuild.get(verifyLivingEntity(context)).getCharacterClass();
        if (characterClass == null) {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".no_class"));
        } else {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".get_class", characterClass.name()));
        }
        return SUCCESS;
    }

    private static int getAbilities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<ResourceLocation> abilityIds = CharacterBuild.get(verifyLivingEntity(context)).getUnlockedAbilityIds();
        if (abilityIds.isEmpty()) {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".no_abilities"));
        } else {
            MutableComponent list = Component.empty();
            boolean empty = true;
            for (ResourceLocation id : abilityIds) {
                Ability ability = MythboundCore.ABILITIES.getData().get(id);
                if (!empty) {
                    list = list.append(", ");
                }
                list = list.append(ability.name());
            }

            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".get_abilities", abilityIds.size(), list));
        }
        return SUCCESS;
    }

    private static int setRace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        Race race = context.getArgument("race", Race.class);

        if (!CharacterBuild.get(entity).setRace(race)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".same_race"));
            return FAILURE;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".set_race", race.name()), false);
            return SUCCESS;
        }
    }

    private static int setClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        CharacterClass characterClass = context.getArgument("class", CharacterClass.class);

        Race race = CharacterBuild.get(entity).getRace();
        if (race == null) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".need_race_to_set_class"));
            return FAILURE;
        } else if (CharacterBuild.notCompatible(race, characterClass)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".incompatible_race_and_class", race.name()));
            return FAILURE;
        } else if (!CharacterBuild.get(entity).setClass(characterClass)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".same_class"));
            return FAILURE;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".set_class", characterClass.name()), false);
            return SUCCESS;
        }
    }

    private static int unlockAbility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        Ability ability = context.getArgument("ability", Ability.class);

        if (!CharacterBuild.get(entity).unlockAbility(ability)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".already_unlocked"));
            return FAILURE;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".unlocked_ability", ability.name()), false);
            return SUCCESS;
        }
    }

    private static int removeAbility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        Ability ability = context.getArgument("ability", Ability.class);

        if (!CharacterBuild.get(entity).removeAbility(ability)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".ability_not_unlocked"));
            return FAILURE;
        } else {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".removed_ability", ability.name()), false);
            return SUCCESS;
        }
    }

    private static int sendInfo(CommandContext<CommandSourceStack> context, NamedAndDescribed obj) {
        context.getSource().sendSystemMessage(obj.name());
        for (Component line : obj.description()) {
            context.getSource().sendSystemMessage(line);
        }
        return SUCCESS;
    }

    private static int reloadBuild(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        CharacterBuild data = CharacterBuild.get(entity);

        Race race = data.getRace();
        if (race != null) {
            race.deinitialize(entity);
            race.initialize(entity);
        }

        CharacterClass characterClass = data.getCharacterClass();
        if (characterClass != null) {
            characterClass.deinitialize(entity);
            characterClass.initialize(entity);
        }

        for (ResourceLocation abilityId : data.getUnlockedAbilityIds()) {
            Ability ability = MythboundCore.ABILITIES.getData().get(abilityId);
            if (ability != null) {
                ability.deinitialize(entity);
                ability.initialize(entity);
            }
        }

        context.getSource().sendSuccess(() -> Component.translatable("commands." + MythboundCore.MODID + ".reloaded_build"), false);
        return SUCCESS;
    }

    //// METHODS

    private static LivingEntity verifyLivingEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        throw ERROR_NOT_LIVING.create();
    }

    @SuppressWarnings("unchecked")
    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> type) {
        return (Class<T>) type;
    }
}
