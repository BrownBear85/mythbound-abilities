package zone.bonker.mb_abilities.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.AbilityType;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.core.ability.TestAbility;

import java.util.function.Supplier;

public class AbilityTypes {
    public static final ResourceKey<Registry<AbilityType<?>>> ABILITY_TYPES_KEY =
            ResourceKey.createRegistryKey(MythboundAbilities.identifier("ability_types"));

    public static final DeferredRegister<AbilityType<?>> ABILITY_TYPES =
            DeferredRegister.create(ABILITY_TYPES_KEY, MythboundAbilities.MODID);

    public static final Registry<AbilityType<?>> REGISTRY = new RegistryBuilder<>(ABILITY_TYPES_KEY).sync(true).create();

    //// ABILITY TYPES

    public static final Supplier<AbilityType<TestAbility>> TEST =
            register("test", TestAbility.CODEC, TestAbility.NETWORK_CODEC);

    //// METHODS

    private static <T extends Ability> Supplier<AbilityType<T>> register(String id, MapCodec<T> codec,
                                                                         StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {
        return ABILITY_TYPES.register(id, () -> new AbilityType<>(codec, networkCodec));
    }
}
