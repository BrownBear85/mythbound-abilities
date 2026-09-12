package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.init.AbilityComponentSerializers;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface AbilityComponent {
    Codec<AbilityComponent> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilityComponentSerializers.REGISTRY.byNameCodec().dispatch(AbilityComponent::codec, Function.identity()));

    MapCodec<? extends AbilityComponent> codec();

    default void onInitialize(LivingEntity entity, ResourceLocation abilityId) {

    }

    default void onDeinitialize(LivingEntity entity, ResourceLocation abilityId) {

    }

    default void onCast(LivingEntity caster, @Nullable LivingEntity target) {

    }

    default void onTick(LivingEntity entity) {

    }
}
