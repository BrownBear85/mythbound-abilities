package zone.bonker.mb_abilities.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.core.Ability;
import zone.bonker.mb_abilities.init.Abilities;
import zone.bonker.mb_abilities.init.AbilityAttachmentTypes;

import javax.annotation.Nullable;
import java.util.*;

public class EntityAbilities {
    private static final ResourceLocation NO_ABILITY = ResourceLocation.withDefaultNamespace("empty");

    public static final Codec<EntityAbilities> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("unlocked").forGetter(o -> new ArrayList<>(o.unlocked)),
            ResourceLocation.CODEC.listOf().fieldOf("equipped").forGetter(o -> List.of(o.equipped))
    ).apply(inst, EntityAbilities::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityAbilities> NETWORK_CODEC =
            StreamCodec.of((buffer, value) -> value.encode(buffer), EntityAbilities::decode);

    private final Set<ResourceLocation> unlocked = new HashSet<>();
    private final ResourceLocation[] equipped;

    public EntityAbilities() {
        equipped = new ResourceLocation[MythboundAbilities.ABILITY_COUNT];
        Arrays.fill(equipped, NO_ABILITY);
    }

    public EntityAbilities(List<ResourceLocation> unlocked, List<ResourceLocation> equipped) {
        this(unlocked, equipped.toArray(ResourceLocation[]::new));
    }

    public EntityAbilities(List<ResourceLocation> unlocked, ResourceLocation[] equipped) {
        this.unlocked.addAll(unlocked);
        this.equipped = equipped;
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(unlocked.size());
        for (ResourceLocation id : unlocked) {
            buffer.writeResourceLocation(id);
        }

        for (ResourceLocation id : equipped) {
            buffer.writeBoolean(id != NO_ABILITY);
            if (id != NO_ABILITY) {
                buffer.writeResourceLocation(id);
            }
        }
    }

    public static EntityAbilities decode(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<ResourceLocation> unlocked = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            unlocked.add(buffer.readResourceLocation());
        }

        ResourceLocation[] abilities = new ResourceLocation[MythboundAbilities.ABILITY_COUNT];
        for (int i = 0; i < abilities.length; i++) {
            abilities[i] = buffer.readBoolean() ? buffer.readResourceLocation() : NO_ABILITY;
        }

        return new EntityAbilities(unlocked, abilities);
    }

    @Nullable
    public static Ability getAbilityInSlot(LivingEntity entity, int index) {
        if (!entity.hasData(AbilityAttachmentTypes.ABILITIES)) {
            return null;
        }

        ResourceLocation id = entity.getData(AbilityAttachmentTypes.ABILITIES).equipped[index];
        if (id == NO_ABILITY) {
            return null;
        }

        return Abilities.getOrThrow(entity.registryAccess(), id);
    }

    public static boolean setAbilityInSlot(LivingEntity entity, int index, ResourceLocation id) {
        EntityAbilities data = entity.getData(AbilityAttachmentTypes.ABILITIES);
        if (!data.equipped[index].equals(id)) {
            data.equipped[index] = id;
            entity.setData(AbilityAttachmentTypes.ABILITIES, data);
            return true;
        } else {
            return false;
        }
    }

    public static boolean unlockAbility(LivingEntity entity, ResourceLocation id) {
        EntityAbilities data = entity.getData(AbilityAttachmentTypes.ABILITIES);
        if (data.unlocked.add(id)) {
            entity.setData(AbilityAttachmentTypes.ABILITIES, data);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUnlocked(LivingEntity entity, ResourceLocation id) {
        if (!entity.hasData(AbilityAttachmentTypes.ABILITIES)) {
            return false;
        }

        return entity.getData(AbilityAttachmentTypes.ABILITIES).unlocked.contains(id);
    }
}
