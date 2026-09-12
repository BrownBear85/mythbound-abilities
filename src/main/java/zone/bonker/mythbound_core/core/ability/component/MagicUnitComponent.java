package zone.bonker.mythbound_core.core.ability.component;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ability.MagicUnitDefinition;
import zone.bonker.mythbound_core.data.SpellData;

import java.util.List;

public record MagicUnitComponent(ResourceLocation unit, List<MagicUnitDefinition.ListenerDefinition<?>> extraListeners) implements AbilityComponent {
    public static final MapCodec<MagicUnitComponent> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("unit").forGetter(MagicUnitComponent::unit),
            MagicUnitDefinition.ListenerDefinition.CODEC.listOf().fieldOf("listeners").forGetter(MagicUnitComponent::extraListeners)
    ).apply(inst, MagicUnitComponent::new));

    @Override
    public MapCodec<? extends AbilityComponent> codec() {
        return CODEC;
    }

    @Override
    public void onInitialize(LivingEntity entity, ResourceLocation abilityId) {
        SpellData.get(entity).registerAssociation(unit, abilityId);

        for (int i = 0; i < extraListeners.size(); i++) {
            extraListeners.get(i).addListener(entity, unit, abilityId.toString(), i);
        }
    }

    @Override
    public void onDeinitialize(LivingEntity entity, ResourceLocation abilityId) {
        for (int i = 0; i < extraListeners.size(); i++) {
            extraListeners.get(i).removeListener(entity, unit, abilityId.toString(), i);
        }
    }
}
