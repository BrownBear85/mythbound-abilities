package zone.bonker.mythbound_core.core.ability.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class TriggerContext {
    public final LivingEntity caster;
    public final Level level;
    @Nullable
    public ResourceLocation spellId;
    @Nullable
    public LivingEntity spellTarget;
    public Vec3 position;
    public Vec2 rotation;

    public TriggerContext(LivingEntity caster) {
        this.caster = caster;
        this.level = caster.level();
        this.position = caster.position();
        this.rotation = caster.getRotationVector();
    }

    public BlockPos blockPosition() {
        return BlockPos.containing(position);
    }
}
