package zone.bonker.mythbound_core.core.ability_effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.init.MythboundEffectSerializers;

import java.util.function.Function;

public abstract class MythboundEffect {
    public static final Codec<MythboundEffect> DIRECT_CODEC =
            Codec.lazyInitialized(() -> MythboundEffectSerializers.REGISTRY.byNameCodec().dispatch(MythboundEffect::codec, Function.identity()));

    public abstract MapCodec<? extends MythboundEffect> codec();

    public void cast(LivingEntity entity) {

    }

    public void tick(LivingEntity entity) {

    }
}
