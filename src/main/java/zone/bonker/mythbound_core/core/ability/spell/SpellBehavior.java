package zone.bonker.mythbound_core.core.ability.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpellBehavior(int windupTicks, int holdTicks, int cooldown, boolean windupCausesCooldown,
                            SpellTargeting targets, SpellCost cost) {
    public static final SpellBehavior DEFAULT = new SpellBehavior(0, 0, 0, true, SpellTargeting.DEFAULT, SpellCost.NO_COST);

    public static final Codec<SpellBehavior> CODEC = RecordCodecBuilder.<SpellBehavior>create(inst -> inst.group(
            Codec.INT.optionalFieldOf("windup_ticks", DEFAULT.windupTicks).forGetter(o -> o.windupTicks),
            Codec.INT.optionalFieldOf("hold_ticks", DEFAULT.holdTicks).forGetter(o -> o.holdTicks),
            Codec.INT.optionalFieldOf("cooldown", DEFAULT.cooldown).forGetter(o -> o.cooldown),
            Codec.BOOL.optionalFieldOf("windup_causes_cooldown", DEFAULT.windupCausesCooldown).forGetter(o -> o.windupCausesCooldown),
            SpellTargeting.CODEC.optionalFieldOf("targeting", DEFAULT.targets).forGetter(o -> o.targets),
            SpellCost.CODEC.optionalFieldOf("cost", DEFAULT.cost).forGetter(o -> o.cost)
    ).apply(inst, SpellBehavior::new)).validate(SpellBehavior::validate);

    private DataResult<SpellBehavior> validate() {
        if (windupTicks < 0) {
            return DataResult.error(() -> "windup_ticks must be >= 0", this);
        }
        if (holdTicks < 0) {
            return DataResult.error(() -> "hold_ticks must be >= 0", this);
        }
        return DataResult.success(this);
    }

    public boolean hasWindup() {
        return windupTicks > 0;
    }

    public boolean hasHoldBehavior() {
        return holdTicks > 0;
    }

    public boolean hasCooldown() {
        return cooldown > 0;
    }

    public boolean isDefault() {
        return equals(DEFAULT);
    }

    public boolean requiresTargetForInitialInput() {
        return hasWindup() ? targets.targetRequiredForWindup() : targets.targetRequiredForCast();
    }
}
