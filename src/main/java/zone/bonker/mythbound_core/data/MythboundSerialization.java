package zone.bonker.mythbound_core.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.function.Supplier;

public class MythboundSerialization {
    public static final Codec<Component> LENIENT_COMPONENT_CODEC = Codec.withAlternative(
            ComponentSerialization.FLAT_CODEC,
            Codec.STRING.xmap(Component::literal, Component::getString));

    public static <T> Codec<T> registryCodec(Supplier<ReloadableJsonRegistry<T>> registrySupplier) {
        return Codec.lazyInitialized(() -> registrySupplier.get().byNameCodec());
    }
}
