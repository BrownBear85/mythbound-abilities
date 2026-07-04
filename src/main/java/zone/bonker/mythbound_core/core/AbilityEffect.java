package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import zone.bonker.mythbound_core.init.AbilityEffectSerializers;

import java.util.function.Function;

public abstract class AbilityEffect {
    public static final Codec<AbilityEffect> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilityEffectSerializers.REGISTRY.byNameCodec().dispatch(AbilityEffect::codec, Function.identity()));

    public abstract MapCodec<? extends AbilityEffect> codec();

    public void onUnlock(Level level, LivingEntity entity) {

    }

    public void onCast(Level level, LivingEntity entity) {

    }

    public void onTick(Level level, LivingEntity entity) {

    }
}
