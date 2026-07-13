package zone.bonker.mythbound_core.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import zone.bonker.mythbound_core.core.ModelProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerModelBuilder {
    private final List<Box> boxes = new ArrayList<>();
    private Box current;

    private PlayerModelBuilder() {
    }
    
    public static PlayerModelBuilder create() {
        return new PlayerModelBuilder();
    }
    
    public PlayerModelBuilder box(String name) {
        current = new Box(name);
        boxes.add(current);
        return this;
    }

    public PlayerModelBuilder get(String name) {
        current = get(name, null);
        return this;
    }

    private Box get(String name, @Nullable Box checkInside) {
        for (Box box : checkInside == null ? boxes : checkInside.children) {
            if (box.name.equals(name)) {
                return box;
            }

            Box found = get(name, box);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public PlayerModelBuilder clone(String name) {
        current = new Box(name, current.xTexOff, current.yTexOff, current.width, current.height, current.depth,
                current.x, current.y, current.z, current.growX, current.growY, current.growZ, current.texScaleU, current.texScaleV);
        boxes.add(current);
        return this;
    }

    public PlayerModelBuilder tex(int xOff, int yOff) {
        current.xTexOff = xOff;
        current.yTexOff = yOff;
        return this;
    }
    
    public PlayerModelBuilder sized(int width, int height, int depth) {
        current.width = width;
        current.height = height;
        current.depth = depth;
        return this;
    }
    
    public PlayerModelBuilder positioned(float x, float y, float z) {
        current.x += x;
        current.y += y;
        current.z += z;
        return this;
    }

    public PlayerModelBuilder pivot(float pivotX, float pivotY, float pivotZ) {
        current.pivotX = pivotX;
        current.pivotY = pivotY;
        current.pivotZ = pivotZ;
        return this;
    }

    public PlayerModelBuilder scale(Optional<ModelProperties.PartProperties> optional) {
        if (optional.isEmpty()) {
            return this;
        }

        ModelProperties.PartProperties properties = optional.get();
        current.growX = current.width * (properties.scaleX() - 1) * 0.5F;
        current.growY = current.height * (properties.scaleY() - 1) * 0.5F;
        current.growZ = current.depth * (properties.scaleZ() - 1) * 0.5F;
        return this;
    }
    
    public PlayerModelBuilder grow(float grow) {
        current.growX += grow;
        current.growY += grow;
        current.growZ += grow;
        return this;
    }

    public PlayerModelBuilder texScale(float texScaleU, float texScaleV) {
        current.texScaleU = texScaleU;
        current.texScaleV = texScaleV;
        return this;
    }
    
    public PlayerModelBuilder addChild(String name) {
        Box child = new Box(name);
        current.children.add(child);
        current = child;
        return this;
    }
    
    public MeshDefinition build() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        for (Box box : boxes) {
            box.build(root);
        }

        return meshDefinition;
    }

    public static class Box {
        public final String name;
        public final List<Box> children = new ArrayList<>();
        public int xTexOff, yTexOff;
        public int width, height, depth;
        public float x, y, z;
        public float pivotX, pivotY, pivotZ;
        public float growX = 0.0F, growY = 0.0F, growZ = 0.0F;
        public float texScaleU = 1.0F, texScaleV = 1.0F;

        public Box(String name) {
            this.name = name;
        }

        public Box(String name, int xTexOff, int yTexOff, int width, int height, int depth,
                   float x, float y, float z, float growX, float growY, float growZ, float texScaleU, float texScaleV) {
            this.name = name;
            this.xTexOff = xTexOff;
            this.yTexOff = yTexOff;
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.x = x;
            this.y = y;
            this.z = z;
            this.growX = growX;
            this.growY = growY;
            this.growZ = growZ;
            this.texScaleU = texScaleU;
            this.texScaleV = texScaleV;
        }

        public void build(PartDefinition partDefinition) {
            PartDefinition thisPart = partDefinition.addOrReplaceChild(name, 
                    CubeListBuilder.create()
                            .texOffs(xTexOff, yTexOff)
                            .addBox(x, y, z, width, height, depth, new CubeDeformation(growX, growY, growZ), texScaleU, texScaleV),
                    PartPose.offset(pivotX, pivotY, pivotZ));
            
            for (Box box : children) {
                box.build(thisPart);
            }
        }
    }
}
