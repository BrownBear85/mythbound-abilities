package zone.bonker.mythbound_core.server;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.data.AbilityReloadListener;
import zone.bonker.mythbound_core.data.EntityAbilities;

import java.util.Arrays;
import java.util.Collection;

public class AbilityArgument implements ArgumentType<Ability> {
    public static final SingletonArgumentInfo<AbilityArgument> INFO = SingletonArgumentInfo.contextFree(AbilityArgument::ability);

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ABILITY = new DynamicCommandExceptionType(
            obj -> Component.translatableEscape("commands." + MythboundCore.MODID + ".unknown_ability", obj)
    );

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL = (context, builder) ->
            SharedSuggestionProvider.suggestResource(AbilityReloadListener.getData().keySet(), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_UNLOCKED = (context, builder) ->
            SharedSuggestionProvider.suggestResource(AbilityReloadListener.getData().keySet()
                    .stream().filter(id -> EntityAbilities.hasAbility(context.getSource().getEntity(), id)), builder);

    public static AbilityArgument ability() {
        return new AbilityArgument();
    }

    @Override
    public Ability parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocation.read(reader);
        Ability ability = AbilityReloadListener.getData().get(id);
        if (ability == null) {
            throw ERROR_UNKNOWN_ABILITY.create(id);
        }
        return ability;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
