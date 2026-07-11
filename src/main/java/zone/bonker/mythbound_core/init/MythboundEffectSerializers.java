package zone.bonker.mythbound_core.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability_effect.MythboundEffect;
import zone.bonker.mythbound_core.core.ability_effect.ShootProjectileEffect;

import java.util.function.Supplier;

public class MythboundEffectSerializers {
    public static final ResourceKey<Registry<MapCodec<? extends MythboundEffect>>> KEY =
            ResourceKey.createRegistryKey(MythboundCore.identifier("effect_serializers"));

    public static final DeferredRegister<MapCodec<? extends MythboundEffect>> REGISTER =
            DeferredRegister.create(KEY, MythboundCore.MODID);

    public static final Registry<MapCodec<? extends MythboundEffect>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).create();

    //// ABILITY TYPES

    public static final Supplier<MapCodec<ShootProjectileEffect>> SHOOT_PROJECTILE =
            register("shoot_projectile", ShootProjectileEffect.CODEC);

    //// METHODS

    private static <T extends MythboundEffect> Supplier<MapCodec<T>> register(String id, MapCodec<T> codec) {
        return REGISTER.register(id, () -> codec);
    }
}
