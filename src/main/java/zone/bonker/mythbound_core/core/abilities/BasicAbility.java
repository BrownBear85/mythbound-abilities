package zone.bonker.mythbound_core.core.abilities;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AbilityEffect;

import java.util.List;

public class BasicAbility extends Ability {
    public static final MapCodec<BasicAbility> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
            .apply(inst, BasicAbility::new));

    private BasicAbility(List<AbilityEffect> effects, Component name, List<Component> description) {
        super(effects, name, description);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return CODEC;
    }
}
