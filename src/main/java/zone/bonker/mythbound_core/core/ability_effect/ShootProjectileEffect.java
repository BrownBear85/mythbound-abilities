package zone.bonker.mythbound_core.core.ability_effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class ShootProjectileEffect extends MythboundEffect {
    public static final MapCodec<ShootProjectileEffect> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CompoundTag.CODEC.fieldOf("projectile").forGetter(o -> o.projectile),
            Codec.FLOAT.fieldOf("velocity").forGetter(o -> o.velocity),
            Codec.FLOAT.optionalFieldOf("loft", 0.0F).forGetter(o -> o.loft),
            Codec.FLOAT.optionalFieldOf("spread", 0.0F).forGetter(o -> o.spread),
            Codec.FLOAT.optionalFieldOf("count", 1.0F).forGetter(o -> o.count)
    ).apply(inst, ShootProjectileEffect::new));

    private final CompoundTag projectile;
    private final float velocity;
    private final float loft;
    private final float spread;
    private final float count;

    public ShootProjectileEffect(CompoundTag projectile, float velocity, float loft, float spread, float count) {
        this.projectile = projectile;
        this.velocity = velocity;
        this.loft = loft;
        this.spread = spread;
        this.count = count;
    }

    @Override
    public MapCodec<? extends MythboundEffect> codec() {
        return CODEC;
    }

    @Override
    public void cast(LivingEntity caster) {
        if (caster.level().isClientSide()) {
            return;
        }

        ResourceLocation entityId = ResourceLocation.tryParse(projectile.getString("id"));
        if (entityId == null) {
            return;
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

        float xRot = caster.getXRot();
        float yRot = caster.getYRot();
        Vec3 angle = new Vec3(
                -Mth.sin(yRot * Mth.DEG_TO_RAD) * Mth.cos(xRot * Mth.DEG_TO_RAD),
                -Mth.sin((xRot + loft) * Mth.DEG_TO_RAD),
                Mth.cos(yRot * Mth.DEG_TO_RAD) * Mth.cos(xRot * Mth.DEG_TO_RAD));

        for (int i = 0; i < count; i++) {
            Entity entity = entityType.create((ServerLevel) caster.level(), e -> {
                e.setPos(caster.getEyePosition());
                if (e instanceof Projectile projEntity) {
                    projEntity.setOwner(caster);
                }
                shoot(e, angle, velocity, spread);
            }, caster.blockPosition(), MobSpawnType.COMMAND, false, false);
            if (entity != null) {
                caster.level().addFreshEntity(entity);
            }
        }
    }

    public void shoot(Entity projectile, Vec3 angle, float velocity, float inaccuracy) {
        Vec3 deltaMovement = angle.normalize()
                .add(
                        projectile.getRandom().triangle(0.0, 0.0172275 * inaccuracy),
                        projectile.getRandom().triangle(0.0, 0.0172275 * inaccuracy),
                        projectile.getRandom().triangle(0.0, 0.0172275 * inaccuracy)
                )
                .scale(velocity);
        projectile.setDeltaMovement(deltaMovement);
        projectile.hasImpulse = true;
        double horizontal = deltaMovement.horizontalDistance();
        projectile.setYRot((float)(Mth.atan2(deltaMovement.x, deltaMovement.z) * Mth.RAD_TO_DEG));
        projectile.setXRot((float)(Mth.atan2(deltaMovement.y, horizontal) * Mth.RAD_TO_DEG));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }
}
