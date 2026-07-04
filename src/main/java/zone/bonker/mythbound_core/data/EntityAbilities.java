package zone.bonker.mythbound_core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.AbilityInputHandler;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AbilityBinding;
import zone.bonker.mythbound_core.init.MBCAttachmentTypes;

import javax.annotation.Nullable;
import java.util.*;

public class EntityAbilities {
    public static final Codec<EntityAbilities> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("abilities").forGetter(o -> o.abilities),
            Codec.unboundedMap(ResourceLocation.CODEC, AbilityBinding.CODEC).fieldOf("bindings").forGetter(o -> o.bindings)
    ).apply(inst, EntityAbilities::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityAbilities> NETWORK_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.abilities,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, AbilityBinding.NETWORK_CODEC), o -> o.bindings,
            EntityAbilities::new);

    private final List<ResourceLocation> abilities = new ArrayList<>();
    private final Map<ResourceLocation, AbilityBinding> bindings = new HashMap<>();

    public EntityAbilities() {

    }

    public EntityAbilities(List<ResourceLocation> abilities, Map<ResourceLocation, AbilityBinding> bindings) {
        this.abilities.addAll(abilities);
        this.bindings.putAll(bindings);
    }

    public static boolean unlockAbility(LivingEntity entity, Ability ability) {
        EntityAbilities data = entity.getData(MBCAttachmentTypes.ABILITIES);
        if (data.abilities.contains(ability.getId())) {
            return false;
        }

        data.abilities.add(ability.getId());
        entity.setData(MBCAttachmentTypes.ABILITIES, data);

        ability.onUnlock(entity.level(), entity);
        return true;
    }

    public static boolean hasAbility(Entity entity, ResourceLocation id) {
        if (!(entity instanceof LivingEntity) || !entity.hasData(MBCAttachmentTypes.ABILITIES)) {
            return false;
        }

        return entity.getData(MBCAttachmentTypes.ABILITIES).abilities.contains(id);
    }

    public static void setBind(LivingEntity entity, ResourceLocation id, AbilityBinding binding) {
        EntityAbilities data = entity.getData(MBCAttachmentTypes.ABILITIES);
        if (!data.abilities.contains(id) || binding.equals(data.bindings.get(id))) {
            return;
        }

        data.bindings.remove(id);
        if (!binding.isUnknown()) {
            data.bindings.put(id, binding);
        }

        entity.setData(MBCAttachmentTypes.ABILITIES, data);
    }

    @Nullable
    public static ResourceLocation getMatchingBoundAbility(LivingEntity entity, int keyCode, int scanCode) {
        if (!entity.hasData(MBCAttachmentTypes.ABILITIES)) {
            return null;
        }

        EntityAbilities data = entity.getData(MBCAttachmentTypes.ABILITIES);
        for (ResourceLocation id : data.bindings.keySet()) {
            AbilityBinding binding = data.bindings.get(id);
            if (AbilityInputHandler.matches(binding, keyCode, scanCode)) {
                return id;
            }
        }

        return null;
    }

    public static void tickUnlockedAbilities(LivingEntity entity) {
        if (!entity.hasData(MBCAttachmentTypes.ABILITIES)) {
            return;
        }

        for (Iterator<ResourceLocation> iterator = entity.getData(MBCAttachmentTypes.ABILITIES).abilities.iterator(); iterator.hasNext(); ) {
            ResourceLocation id = iterator.next();
            Ability ability = AbilityReloadListener.getData().get(id);
            if (ability == null) {
                MythboundCore.LOGGER.warn("Tried to tick unregistered ability {}, removing from entity {}", id, entity);
                iterator.remove();
            } else {
                ability.onTick(entity.level(), entity);
            }
        }
    }
}
