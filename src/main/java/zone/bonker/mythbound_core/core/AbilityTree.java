package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;

public record AbilityTree() {
    public static final Codec<AbilityTree> CODEC = Codec.unit(new AbilityTree());

    public void initialize() {

    }

    public void deinitialize() {

    }
}
