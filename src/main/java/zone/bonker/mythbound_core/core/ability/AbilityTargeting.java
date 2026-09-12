package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

import javax.annotation.Nullable;

public record AbilityTargeting(boolean allowEnemies, boolean allowAllies, boolean allowSelf, double maxRange, boolean targetRequired) {
    public static final AbilityTargeting DEFAULT = new AbilityTargeting(false, false, false, 100.0, false);

    public static final Codec<AbilityTargeting> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("enemies", DEFAULT.allowEnemies).forGetter(AbilityTargeting::allowEnemies),
            Codec.BOOL.optionalFieldOf("allies", DEFAULT.allowAllies).forGetter(AbilityTargeting::allowAllies),
            Codec.BOOL.optionalFieldOf("self", DEFAULT.allowSelf).forGetter(AbilityTargeting::allowSelf),
            Codec.DOUBLE.optionalFieldOf("range", DEFAULT.maxRange).forGetter(AbilityTargeting::maxRange),
            Codec.BOOL.optionalFieldOf("required", DEFAULT.targetRequired).forGetter(AbilityTargeting::targetRequired)
    ).apply(inst, AbilityTargeting::new));

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
