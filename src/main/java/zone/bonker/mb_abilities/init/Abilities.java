package zone.bonker.mb_abilities.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.AbilityType;
import zone.bonker.mb_abilities.core.Ability;

public class Abilities {
    public static final ResourceKey<Registry<Ability>> ABILITIES_KEY =
            ResourceKey.createRegistryKey(MythboundAbilities.identifier("abilities"));
    public static final Codec<Ability> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilityTypes.REGISTRY.byNameCodec().dispatch(Ability::getType, AbilityType::codec));

    public static Ability getOrThrow(RegistryAccess registryAccess, ResourceLocation id) {
        Ability ability = registryAccess.registryOrThrow(ABILITIES_KEY).get(id);
        if (id == null) {
            throw new IllegalArgumentException("Unregistered ability id: " + id);
        }
        return ability;
    }

    public static ResourceLocation getKeyOrThrow(RegistryAccess registryAccess, Ability ability) {
        ResourceLocation id = registryAccess.registryOrThrow(ABILITIES_KEY).getKey(ability);
        if (id == null) {
            throw new IllegalArgumentException("Unregistered ability: " + ability);
        }
        return id;
    }
}
