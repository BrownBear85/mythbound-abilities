package zone.bonker.mythbound_core.server;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.data.ReloadableJsonRegistry;

import java.util.Arrays;
import java.util.Collection;

public class MythboundRegistryArgument<T> implements ArgumentType<T> {
    public static final Info<?> INFO = new Info<>();

    private static final DynamicCommandExceptionType ERROR_UNREGISTERED_ID = new DynamicCommandExceptionType(
            obj -> Component.translatableEscape("commands." + MythboundCore.MODID + ".unregistered_id", obj));

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_RACES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MythboundCore.RACES.getData().keySet(), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_CLASSES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MythboundCore.CLASSES.getData().keySet(), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ABILITIES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MythboundCore.ABILITIES.getData().keySet(), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_UNLOCKED_ABILITIES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(MythboundCore.ABILITIES.getData().keySet()
                    .stream().filter(id -> CharacterBuild.getExisting(context.getSource().getEntity()).map(data -> data.hasAbility(id)).orElse(true)), builder);

    private final ReloadableJsonRegistry<T> registry;

    public MythboundRegistryArgument(ReloadableJsonRegistry<T> registry) {
        this.registry = registry;
    }

    public static MythboundRegistryArgument<Race> race() {
        return new MythboundRegistryArgument<>(MythboundCore.RACES);
    }

    public static MythboundRegistryArgument<CharacterClass> characterClass() {
        return new MythboundRegistryArgument<>(MythboundCore.CLASSES);
    }

    public static MythboundRegistryArgument<Ability> ability() {
        return new MythboundRegistryArgument<>(MythboundCore.ABILITIES);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocation.read(reader);
        T obj = registry.getData().get(id);
        if (obj == null) {
            throw ERROR_UNREGISTERED_ID.create(id);
        }
        return obj;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info<T> implements ArgumentTypeInfo<MythboundRegistryArgument<T>, Info<T>.Template> {
        public void serializeToNetwork(Info<T>.Template template, FriendlyByteBuf buffer) {
            buffer.writeUtf(template.registry.getDirectory());
        }

        @SuppressWarnings("unchecked")
        public Info<T>.Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            return new Info<T>.Template((ReloadableJsonRegistry<T>) MythboundCore.REGISTRIES.getRegistry(buffer.readUtf()));
        }

        public void serializeToJson(Info<T>.Template template, JsonObject json) {
            json.addProperty("registry", template.registry.getDirectory());
        }

        public Info<T>.Template unpack(MythboundRegistryArgument<T> argument) {
            return new Info<T>.Template(argument.registry);
        }

        public final class Template implements ArgumentTypeInfo.Template<MythboundRegistryArgument<T>> {
            final ReloadableJsonRegistry<T> registry;

            Template(ReloadableJsonRegistry<T> registry) {
                this.registry = registry;
            }

            public MythboundRegistryArgument<T> instantiate(CommandBuildContext context) {
                return new MythboundRegistryArgument<>(this.registry);
            }

            @Override
            public ArgumentTypeInfo<MythboundRegistryArgument<T>, ?> type() {
                return Info.this;
            }
        }
    }
}
