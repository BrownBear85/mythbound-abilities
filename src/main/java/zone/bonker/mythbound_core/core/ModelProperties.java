package zone.bonker.mythbound_core.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ModelProperties(float hitboxScaleX, float hitboxScaleY,
                              Optional<PartProperties> legs, Optional<PartProperties> arms,
                              Optional<PartProperties> body, Optional<PartProperties> head) {

    public static final ModelProperties DEFAULT = new ModelProperties(1.0F, 1.0F,
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<ModelProperties> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("hitbox_scale_x", 1.0F).forGetter(ModelProperties::hitboxScaleX),
            Codec.FLOAT.optionalFieldOf("hitbox_scale_y", 1.0F).forGetter(ModelProperties::hitboxScaleY),
            PartProperties.CODEC.optionalFieldOf("legs").forGetter(ModelProperties::legs),
            PartProperties.CODEC.optionalFieldOf("arms").forGetter(ModelProperties::arms),
            PartProperties.CODEC.optionalFieldOf("body").forGetter(ModelProperties::body),
            PartProperties.CODEC.optionalFieldOf("head").forGetter(ModelProperties::head)
    ).apply(inst, ModelProperties::new));

    public boolean hasCustomHitbox() {
        return hitboxScaleX != 1.0F || hitboxScaleY != 1.0F;
    }

    public boolean hasCustomProportions() {
        return legs.isPresent() || arms.isPresent() || body.isPresent() || head.isPresent();
    }

    public record PartProperties(float scaleX, float scaleY, float scaleZ) {
        public static final Codec<PartProperties> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("x_scale", 1.0F).forGetter(PartProperties::scaleX),
                Codec.FLOAT.optionalFieldOf("y_scale", 1.0F).forGetter(PartProperties::scaleY),
                Codec.FLOAT.optionalFieldOf("z_scale", 1.0F).forGetter(PartProperties::scaleZ)
        ).apply(inst, PartProperties::new));
    }

    public interface ModelPropContainer {
        ModelProperties modelProperties();

        ResourceLocation getId();
    }
}
