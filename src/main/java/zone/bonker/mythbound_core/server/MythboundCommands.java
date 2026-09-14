package zone.bonker.mythbound_core.server;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
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
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;

import java.util.List;
import java.util.Set;

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
                                        .executes(MythboundCommands::setClass)))
                        .then(Commands.literal("subclass")
                                .then(Commands.argument("subclass", ResourceLocationArgument.id())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggestResource(CharacterBuild.getExisting(context.getSource().getEntity())
                                                        .map(CharacterBuild::getCharacterClass)
                                                        .map(characterClass -> characterClass.subclasses().keySet())
                                                        .orElse(Set.of()), builder))
                                        .executes(MythboundCommands::setSubclass)))
                        .then(Commands.literal("points")
                                .then(Commands.argument("class_points", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("subclass_points", IntegerArgumentType.integer(0))
                                            .executes(MythboundCommands::setPoints)))))
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
                .then(Commands.literal("reset")
                        .executes(MythboundCommands::resetBuild)));
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
        List<ResourceLocation> abilityIds = CharacterBuild.get(verifyLivingEntity(context)).getUnlockedAbilities();
        if (abilityIds.isEmpty()) {
            context.getSource().sendSystemMessage(Component.translatable("commands." + MythboundCore.MODID + ".no_abilities"));
        } else {
            MutableComponent list = Component.empty();
            boolean empty = true;
            for (ResourceLocation id : abilityIds) {
                Ability ability = MythboundCore.ABILITIES.getOrThrow(id);
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

        if (CharacterBuild.get(entity).getRace() == race) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".same_race"));
            return FAILURE;
        } else {
            CharacterBuild.get(entity).setRace(race);
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
        } else if (CharacterBuild.get(entity).getCharacterClass() == characterClass) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".same_class"));
            return FAILURE;
        } else {
            CharacterBuild.get(entity).setClass(characterClass);
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands." + MythboundCore.MODID + ".set_class", characterClass.name()), false);
            return SUCCESS;
        }
    }

    private static int setSubclass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        ResourceLocation subclassId = context.getArgument("subclass", ResourceLocation.class);

        CharacterClass characterClass = CharacterBuild.get(entity).getCharacterClass();
        if (characterClass == null) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".need_class_to_set_subclass"));
            return FAILURE;
        } else if (!characterClass.subclasses().containsKey(subclassId)) {
            context.getSource().sendFailure(Component.translatable("commands." + MythboundCore.MODID + ".invalid_subclass", characterClass.name(), subclassId.toString()));
            return FAILURE;
        }

        CharacterBuild.get(entity).setSubclass(subclassId);
        context.getSource().sendSuccess(() ->
                Component.translatable("commands." + MythboundCore.MODID + ".set_subclass", characterClass.subclasses().get(subclassId).name()), false);
        return SUCCESS;
    }

    private static int setPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        int classPoints = context.getArgument("class_points", int.class);
        int subclassPoints = context.getArgument("subclass_points", int.class);

        CharacterBuild.get(entity).setPoints(classPoints, subclassPoints);
        return SUCCESS;
    }

    private static int sendInfo(CommandContext<CommandSourceStack> context, NamedAndDescribed obj) {
        context.getSource().sendSystemMessage(obj.name());
        for (Component line : obj.description()) {
            context.getSource().sendSystemMessage(line);
        }
        return SUCCESS;
    }

    private static int resetBuild(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LivingEntity entity = verifyLivingEntity(context);
        entity.removeData(MythboundAttachmentTypes.CHARACTER_BUILD);
        CharacterBuild.get(entity);

        context.getSource().sendSuccess(() -> Component.translatable("commands." + MythboundCore.MODID + ".reset_build"), false);
        return 1;
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
