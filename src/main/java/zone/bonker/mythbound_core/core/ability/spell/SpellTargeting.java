package zone.bonker.mythbound_core.core.ability.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

import javax.annotation.Nullable;

public record SpellTargeting(boolean allowEnemies, boolean allowAllies, boolean allowSelf, double maxRange, boolean targetRequiredForWindup, boolean targetRequiredForCast) {
    public static final SpellTargeting DEFAULT = new SpellTargeting(false, false, false, 100.0, false, false);

    public static final Codec<SpellTargeting> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("enemies", DEFAULT.allowEnemies).forGetter(SpellTargeting::allowEnemies),
            Codec.BOOL.optionalFieldOf("allies", DEFAULT.allowAllies).forGetter(SpellTargeting::allowAllies),
            Codec.BOOL.optionalFieldOf("self", DEFAULT.allowSelf).forGetter(SpellTargeting::allowSelf),
            Codec.DOUBLE.optionalFieldOf("range", DEFAULT.maxRange).forGetter(SpellTargeting::maxRange),
            Codec.BOOL.optionalFieldOf("required_for_windup", DEFAULT.targetRequiredForWindup).forGetter(SpellTargeting::targetRequiredForWindup),
            Codec.BOOL.optionalFieldOf("required_to_cast", DEFAULT.targetRequiredForCast).forGetter(SpellTargeting::targetRequiredForCast)
    ).apply(inst, SpellTargeting::new));

    public boolean isEmpty() {
        return !allowEnemies && !allowAllies && !allowSelf;
    }

    public boolean matches(LivingEntity target, @Nullable LivingEntity caster) {
        if (allowSelf && caster != null && target == caster) {
            return true;
        } else if (allowAllies && isAlly(target)) {
            return true;
        } else {
            return allowEnemies;
        }
    }

    public static boolean isAlly(LivingEntity target) {
        // TODO: implement ally criteria
        return target instanceof Villager;
//        return false;
    }
}
