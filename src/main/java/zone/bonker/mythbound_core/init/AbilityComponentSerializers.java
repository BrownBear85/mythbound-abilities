package zone.bonker.mythbound_core.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.component.AbilityComponent;
import zone.bonker.mythbound_core.core.ability.component.MagicUnitComponent;
import zone.bonker.mythbound_core.core.ability.component.ShootProjectileComponent;

import java.util.function.Supplier;

public class AbilityComponentSerializers {
    public static final ResourceKey<Registry<MapCodec<? extends AbilityComponent>>> KEY =
            ResourceKey.createRegistryKey(MythboundCore.identifier("ability_components"));

    public static final DeferredRegister<MapCodec<? extends AbilityComponent>> REGISTER =
            DeferredRegister.create(KEY, MythboundCore.MODID);

    public static final Registry<MapCodec<? extends AbilityComponent>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).create();

    public static final Supplier<MapCodec<ShootProjectileComponent>> SHOOT_PROJECTILE =
            REGISTER.register("shoot_projectile", () -> ShootProjectileComponent.CODEC);

    public static final Supplier<MapCodec<MagicUnitComponent>> MAGIC_UNIT =
            REGISTER.register("magic_unit", () -> MagicUnitComponent.CODEC);
}
