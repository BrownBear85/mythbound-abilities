package zone.bonker.mythbound_core.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MythboundSerialization {
    public static final Codec<Component> LENIENT_COMPONENT_CODEC = Codec.withAlternative(
            ComponentSerialization.FLAT_CODEC,
            Codec.STRING.xmap(Component::literal, Component::getString));

    public static final Codec<Integer> COLOR_CODEC = Codec.withAlternative(Codec.INT,
            Codec.of(Codec.INT::encode, Codec.STRING.flatMap(MythboundSerialization::parseColor)));

    public static final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, JsonElement>> RESOURCE_LIST_CODEC =
            ByteBufCodecs.map(HashMap::new,
                    ResourceLocation.STREAM_CODEC,
                    ByteBufCodecs.STRING_UTF8.map(JsonParser::parseString, MythboundReloadableRegistries.GSON::toJson));

    private static DataResult<Integer> parseColor(String str) {
        final String input = str;

        if (str.startsWith("#")) {
            str = str.substring(1);
        }

        try {
            return DataResult.success(Integer.parseUnsignedInt(str, 16));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a hex color string: '" + input + "'");
        }
    }

    public static <T> Codec<T> registryCodec(Supplier<ReloadableJsonRegistry<T>> registrySupplier) {
        return Codec.lazyInitialized(() -> registrySupplier.get().byNameCodec());
    }
}
