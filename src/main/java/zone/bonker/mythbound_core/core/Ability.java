package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability_effect.MythboundEffect;
import zone.bonker.mythbound_core.data.MythboundSerialization;
import zone.bonker.mythbound_core.networking.S2CEntityAbilityPacket;

import java.util.List;

public record Ability(Component name, List<Component> description, List<MythboundEffect> effects,
                      AttributeList attributes) implements NamedAndDescribed {

    public static final Codec<Ability> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MythboundSerialization.LENIENT_COMPONENT_CODEC.fieldOf("name").forGetter(Ability::name),
            MythboundSerialization.LENIENT_COMPONENT_CODEC.listOf().fieldOf("description").forGetter(Ability::description),
            MythboundEffect.DIRECT_CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(Ability::effects),
            AttributeList.CODEC.optionalFieldOf("attributes", AttributeList.EMPTY).forGetter(Ability::attributes)
    ).apply(inst, Ability::new));

    public ResourceLocation getId() {
        return MythboundCore.ABILITIES.getData().inverse().get(this);
    }

    public void tryCast(LivingEntity caster) {
        cast(caster);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new S2CEntityAbilityPacket(caster.getId(), getId()));
    }

    public void initialize(LivingEntity entity) {
        attributes.apply(entity, AttributeList.ability(getId()));
    }

    public void deinitialize(LivingEntity entity) {
        attributes.remove(entity, AttributeList.ability(getId()));
    }

    public void cast(LivingEntity entity) {
        for (MythboundEffect effect : effects) {
            effect.cast(entity);
        }
    }
}
