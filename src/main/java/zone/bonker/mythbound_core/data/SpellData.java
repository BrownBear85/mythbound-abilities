package zone.bonker.mythbound_core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.MagicUnitDefinition;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;

import javax.annotation.Nullable;
import java.util.*;

public class SpellData extends OwnedAttachment {
    public static final Codec<SpellData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("unit_values").forGetter(o -> o.unitValues)
    ).apply(inst, SpellData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellData> NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), o -> o.unitValues,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ResourceLocation.STREAM_CODEC), o -> o.associationMap,
            SpellData::new);

    private final Map<ResourceLocation, Integer> unitValues = new HashMap<>();
    // key = unit id, value = spell id
    private final Map<ResourceLocation, ResourceLocation> associationMap = new HashMap<>();

    // New data
    public SpellData(IAttachmentHolder holder) {
        super(holder);
    }

    // From disc
    private SpellData(Map<ResourceLocation, Integer> unitValues) {
        this.unitValues.putAll(unitValues);
    }

    private SpellData(Map<ResourceLocation, Integer> unitValues, Map<ResourceLocation, ResourceLocation> associationMap) {
        this(unitValues);
        this.associationMap.putAll(associationMap);
    }

    public static SpellData get(LivingEntity entity) {
        return entity.getData(MythboundAttachmentTypes.SPELL_DATA);
    }

    public int getUnitAmount(ResourceLocation unit) {
        if (!unitValues.containsKey(unit)) {
            return 0;
        }
        return unitValues.get(unit);
    }

    public void setUnitAmount(ResourceLocation unit, int amount) {
        MagicUnitDefinition unitDefinition = MythboundCore.MAGIC_UNITS.getOrThrow(unit);

        if (amount == 0) {
            unitValues.remove(unit);
        } else {
            unitValues.put(unit, Mth.clamp(amount, 0, unitDefinition.maximum()));
        }

        save();
    }

    public void addUnits(ResourceLocation unit, int amount) {
        setUnitAmount(unit, getUnitAmount(unit) + amount);
    }

    public void registerAssociation(ResourceLocation unitId, ResourceLocation abilityId) {
        associationMap.put(unitId, abilityId);
        save();
    }

    @Nullable
    public ResourceLocation getAssociation(ResourceLocation unitId) {
        return associationMap.get(unitId);
    }

    private void save() {
        owner().setData(MythboundAttachmentTypes.SPELL_DATA, this);
    }
}
