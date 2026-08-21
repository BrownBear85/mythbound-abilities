package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.init.AbilityComponentSerializers;

import java.util.function.Function;

public abstract class AbilityComponent {
    public static final Codec<AbilityComponent> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilityComponentSerializers.REGISTRY.byNameCodec().dispatch(AbilityComponent::codec, Function.identity()));

    public abstract MapCodec<? extends AbilityComponent> codec();

    public void cast(LivingEntity entity) {

    }

    public void tick(LivingEntity entity) {

    }
}
