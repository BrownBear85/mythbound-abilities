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
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.spell.MagicUnitDefinition;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;
import zone.bonker.mythbound_core.init.MythboundTriggers;

import javax.annotation.Nullable;
import java.util.*;

public class SpellData extends OwnedAttachment {
    public static final Codec<SpellData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("unit_values").forGetter(o -> o.unitValues)
    ).apply(inst, SpellData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellData> NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), o -> o.unitValues,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ResourceLocation.STREAM_CODEC), o -> o.associationMap,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), o -> o.cooldownMap,
            ResourceLocation.STREAM_CODEC, o -> o.currentSpell,
            ByteBufCodecs.VAR_LONG, o -> o.spellStartTime,
            ByteBufCodecs.BOOL, o -> o.alreadyCast,
            SpellData::new);

    private final Map<ResourceLocation, Integer> unitValues = new HashMap<>();
    // key = unit id, value = spell id
    private final Map<ResourceLocation, ResourceLocation> associationMap = new HashMap<>();

    private final Map<ResourceLocation, Integer> cooldownMap = new HashMap<>();

    private ResourceLocation currentSpell = CharacterBuild.NONE;
    @Nullable
    private LivingEntity currentTarget;
    private long spellStartTime = -1;
    private boolean alreadyCast = false;

    // New data
    public SpellData(IAttachmentHolder holder) {
        super(holder);
    }

    // From disc
    private SpellData(Map<ResourceLocation, Integer> unitValues) {
        this.unitValues.putAll(unitValues);
    }

    private SpellData(Map<ResourceLocation, Integer> unitValues, Map<ResourceLocation, ResourceLocation> associationMap,
                      Map<ResourceLocation, Integer> cooldownMap, ResourceLocation currentSpell,
                      long spellStartTime, boolean alreadyCast) {
        this(unitValues);
        this.associationMap.putAll(associationMap);
        this.cooldownMap.putAll(cooldownMap);
        this.currentSpell = currentSpell;
        this.spellStartTime = spellStartTime;
        this.alreadyCast = alreadyCast;
    }

    public static SpellData get(LivingEntity entity) {
        return entity.getData(MythboundAttachmentTypes.SPELL_DATA);
    }

    public static Optional<SpellData> getExisting(LivingEntity entity) {
        return entity.getExistingData(MythboundAttachmentTypes.SPELL_DATA);
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
            unitValues.put(unit, Mth.clamp(amount, unitDefinition.minimum(), unitDefinition.maximum()));
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

    private void setOnCooldown(Ability spell) {
        if (spell.spellBehavior().hasCooldown()) {
            cooldownMap.put(spell.getId(), spell.spellBehavior().cooldown());
            save();
        }
    }

    public boolean isOnCooldown(ResourceLocation spellId) {
        return cooldownMap.containsKey(spellId);
    }

    public float getCooldownAmount(Ability spell, float partialTick) {
        if (!spell.spellBehavior().hasCooldown() || !isOnCooldown(spell.getId())) {
            return 0;
        }
        return (cooldownMap.get(spell.getId()) - partialTick) / spell.spellBehavior().cooldown();
    }

    public void startCastingSpell(ResourceLocation abilityId, @Nullable LivingEntity target, long timestamp, boolean alreadyCast) {
        if (!alreadyCast) {
            MythboundTriggers.SPELL_WINDUP_START.get().trigger(owner(), abilityId, target, 0);
        }

        this.currentSpell = abilityId;
        this.currentTarget = target;
        this.spellStartTime = timestamp;
        this.alreadyCast = alreadyCast;
        save();
    }

    public void stopCastingSpell(long timestamp) {
        (alreadyCast ? MythboundTriggers.SPELL_HOLD_END : MythboundTriggers.SPELL_WINDUP_RELEASE).get().trigger(owner(), currentSpell, currentTarget, (int) (timestamp - spellStartTime));

        Ability ability = MythboundCore.ABILITIES.getOrThrow(currentSpell);
        if (alreadyCast || ability.spellBehavior().windupCausesCooldown()) {
            setOnCooldown(ability);
        }

        this.currentSpell = CharacterBuild.NONE;
        this.currentTarget = null;
        this.spellStartTime = -1;
        this.alreadyCast = false;
        save();
    }

    public boolean isCastingSpell() {
        return !currentSpell.equals(CharacterBuild.NONE);
    }

    public ResourceLocation getCurrentSpell() {
        return currentSpell;
    }

    public void startHoldingOrInstantCast(Ability spell, @Nullable LivingEntity target, long timestamp) {
        if (spell.spellBehavior().hasWindup()) {
            startCastingSpell(spell.getId(), target, timestamp, false);
        } else if (!owner().level().isClientSide) {
            castSpell(spell, target);
            if (spell.spellBehavior().hasHoldBehavior()) {
                startCastingSpell(spell.getId(), target, timestamp, true);
            } else {
                setOnCooldown(spell);
            }
        }
    }

    private void castSpell(Ability spell, @Nullable LivingEntity target) {
        MythboundTriggers.SPELL_CAST.get().trigger(owner(), spell.getId(), target, 0);
        spell.cost().consumeUnits(owner());
    }

    public void setTarget(@Nullable LivingEntity newTarget) {
        if (isCastingSpell()) {
            currentTarget = newTarget;

            if (!MythboundCore.ABILITIES.getOrThrow(currentSpell).targeting().matches(currentTarget, owner())) {
                stopCastingSpell(owner().level().getGameTime());
            } else {
                save();
            }
        }
    }

    public void tick() {
        for (Map.Entry<ResourceLocation, Integer> entry : new HashSet<>(cooldownMap.entrySet())) {
            if (entry.getValue() <= 1) {
                cooldownMap.remove(entry.getKey());
            } else {
                cooldownMap.put(entry.getKey(), entry.getValue() - 1);
            }
        }

        if (!isCastingSpell()) {
            return;
        }

        Ability spell = MythboundCore.ABILITIES.getOrThrow(currentSpell);

        long timestamp = owner().level().getGameTime();
        long ticks = timestamp - spellStartTime;

        if (!spell.cost().canCast(owner())) {
            stopCastingSpell(timestamp);
            return;
        }

        if (alreadyCast && ticks > spell.spellBehavior().holdTicks()) {
            stopCastingSpell(timestamp);
        } else if (!alreadyCast && ticks > spell.spellBehavior().windupTicks()) {
            castSpell(spell, currentTarget);
            if (spell.spellBehavior().hasHoldBehavior()) {
                startCastingSpell(currentSpell, currentTarget, timestamp, true);
            } else {
                stopCastingSpell(timestamp);
            }
        } else {
            (alreadyCast ? MythboundTriggers.SPELL_HOLD_TICK : MythboundTriggers.SPELL_WINDUP_TICK).get().trigger(owner(), currentSpell, currentTarget, (int) ticks);
        }
    }

    private void save() {
        owner().setData(MythboundAttachmentTypes.SPELL_DATA, this);
    }
}
