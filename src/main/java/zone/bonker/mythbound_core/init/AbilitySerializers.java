package zone.bonker.mythbound_core.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.abilities.BasicAbility;

import java.util.function.Supplier;

public class AbilitySerializers {
    public static final ResourceKey<Registry<MapCodec<? extends Ability>>> KEY =
            ResourceKey.createRegistryKey(MythboundCore.identifier("ability_serializers"));

    public static final DeferredRegister<MapCodec<? extends Ability>> REGISTER =
            DeferredRegister.create(KEY, MythboundCore.MODID);

    public static final Registry<MapCodec<? extends Ability>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).create();

    //// ABILITY SERIALIZERS

    public static final Supplier<MapCodec<BasicAbility>> BASIC = register("basic", BasicAbility.CODEC);

    //// METHODS

    private static <T extends Ability> Supplier<MapCodec<T>> register(String id, MapCodec<T> codec) {
        return REGISTER.register(id, () -> codec);
    }
}
