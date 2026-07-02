package zone.bonker.mb_abilities.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record AbilityType<T extends Ability>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {

}
