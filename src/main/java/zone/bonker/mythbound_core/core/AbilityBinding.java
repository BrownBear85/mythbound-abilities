package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AbilityBinding(int key, byte type, boolean shift, boolean control, boolean alt) {
    public static final Codec<AbilityBinding> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("key").forGetter(AbilityBinding::key),
            Codec.BYTE.fieldOf("type").forGetter(o -> o.type),
            Codec.BOOL.fieldOf("shift").forGetter(AbilityBinding::shift),
            Codec.BOOL.fieldOf("control").forGetter(AbilityBinding::control),
            Codec.BOOL.fieldOf("alt").forGetter(AbilityBinding::alt)
    ).apply(inst, AbilityBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityBinding> NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbilityBinding::key,
            ByteBufCodecs.BYTE, AbilityBinding::type,
            ByteBufCodecs.BOOL, AbilityBinding::shift,
            ByteBufCodecs.BOOL, AbilityBinding::control,
            ByteBufCodecs.BOOL, AbilityBinding::alt,
            AbilityBinding::new
    );

    public boolean isUnknown() {
        return key == -1;
    }
}
