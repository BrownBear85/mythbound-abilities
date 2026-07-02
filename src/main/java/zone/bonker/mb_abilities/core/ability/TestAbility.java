package zone.bonker.mb_abilities.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import zone.bonker.mb_abilities.core.AbilityType;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.init.AbilityTypes;

public class TestAbility extends Ability {
    public static final MapCodec<TestAbility> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("message").forGetter(o -> o.message)
    ).apply(inst, TestAbility::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TestAbility> NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, o -> o.message,
            TestAbility::new);

    private final String message;

    public TestAbility(String message) {
        this.message = message;
    }

    @Override
    public AbilityType<?> getType() {
        return AbilityTypes.TEST.get();
    }

    @Override
    public void castServer(ServerLevel level, LivingEntity caster) {
        if (caster instanceof Player player) {
            player.sendSystemMessage(Component.literal("from " + Thread.currentThread().getName() + ": " + message));
        }
    }

    @Override
    public void castClient(Level level, LivingEntity caster) {
        if (caster instanceof Player player) {
            player.sendSystemMessage(Component.literal("from " + Thread.currentThread().getName() + ": " + message));
        }
    }
}
