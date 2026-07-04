package zone.bonker.mythbound_core.core;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mythbound_core.data.AbilityReloadListener;
import zone.bonker.mythbound_core.init.AbilitySerializers;
import zone.bonker.mythbound_core.networking.S2CEntityAbilityPacket;

import java.util.List;
import java.util.function.Function;

public abstract class Ability {
    public static final Codec<Ability> DIRECT_CODEC =
            Codec.lazyInitialized(() -> AbilitySerializers.REGISTRY.byNameCodec().dispatch(Ability::codec, Function.identity()));

    private final List<AbilityEffect> effects;
    private final Component name;
    private final List<Component> description;
    private ResourceLocation id;

    protected Ability(List<AbilityEffect> effects, Component name, List<Component> description) {
        this.effects = effects;
        this.name = name;
        this.description = description;
    }

    protected static <T extends Ability> Products.P3<RecordCodecBuilder.Mu<T>, List<AbilityEffect>, Component, List<Component>> codecStart(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                AbilityEffect.DIRECT_CODEC.listOf().fieldOf("effects").forGetter(Ability::getPassiveEffects),
                ComponentSerialization.FLAT_CODEC.fieldOf("name").forGetter(Ability::getName),
                ComponentSerialization.FLAT_CODEC.listOf().fieldOf("description").forGetter(Ability::getDescription)
        );
    }

    public abstract MapCodec<? extends Ability> codec();

    public List<AbilityEffect> getPassiveEffects() {
        return effects;
    }

    public final Component getName() {
        return name;
    }

    public final List<Component> getDescription() {
        return description;
    }

    public final ResourceLocation getId() {
        if (id == null) {
            id = AbilityReloadListener.getData().inverse().get(this);
        }
        return id;
    }

    public void tryCast(ServerLevel level, LivingEntity caster) {
        if (canCast(level, caster)) {
            onCast(level, caster);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new S2CEntityAbilityPacket(caster.getId(), id));
        }
    }

    public boolean canCast(Level level, LivingEntity caster) {
        return true;
    }

    public void onUnlock(Level level, LivingEntity entity) {
        for (AbilityEffect effect : effects) {
            effect.onUnlock(level, entity);
        }
    }

    public void onCast(Level level, LivingEntity entity) {
        for (AbilityEffect effect : effects) {
            effect.onCast(level, entity);
        }
    }

    public void onTick(Level level, LivingEntity entity) {
        for (AbilityEffect effect : effects) {
            effect.onTick(level, entity);
        }
    }
}
