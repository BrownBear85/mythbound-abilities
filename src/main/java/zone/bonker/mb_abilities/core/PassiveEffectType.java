package zone.bonker.mb_abilities.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PassiveEffectType<T extends PassiveEffect>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {
}
