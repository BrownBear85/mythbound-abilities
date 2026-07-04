package zone.bonker.mythbound_core.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.AbilityEffect;
import zone.bonker.mythbound_core.core.passive_effects.PermanentAttributeEffect;
import zone.bonker.mythbound_core.core.passive_effects.ShootProjectileEffect;

import java.util.function.Supplier;

public class AbilityEffectSerializers {
    public static final ResourceKey<Registry<MapCodec<? extends AbilityEffect>>> KEY =
            ResourceKey.createRegistryKey(MythboundCore.identifier("ability_effect_serializers"));

    public static final DeferredRegister<MapCodec<? extends AbilityEffect>> REGISTER =
            DeferredRegister.create(KEY, MythboundCore.MODID);

    public static final Registry<MapCodec<? extends AbilityEffect>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).create();

    //// ABILITY TYPES

    public static final Supplier<MapCodec<PermanentAttributeEffect>> PERMANENT_ATTRIBUTE =
            register("permanent_attribute", PermanentAttributeEffect.CODEC);

    public static final Supplier<MapCodec<ShootProjectileEffect>> SHOOT_PROJECTILE =
            register("shoot_projectile", ShootProjectileEffect.CODEC);

    //// METHODS

    private static <T extends AbilityEffect> Supplier<MapCodec<T>> register(String id, MapCodec<T> codec) {
        return REGISTER.register(id, () -> codec);
    }
}
