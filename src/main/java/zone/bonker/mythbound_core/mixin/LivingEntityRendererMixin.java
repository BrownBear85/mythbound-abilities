package zone.bonker.mythbound_core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.Optional;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                    ordinal = 0))
    public void render(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!(entity instanceof Player)) {
            return;
        }

        Optional<CharacterBuild> optional = CharacterBuild.getExisting(entity);
        if (optional.isEmpty()) {
            return;
        }

        Race race = optional.get().getRace();
        if (race == null || race.modelProperties().isDefault()) {
            return;
        }
        float xScale = race.modelProperties().widthScale();
        float yScale = race.modelProperties().heightScale();
        float zScale = race.modelProperties().depthScale();

        poseStack.scale(xScale, yScale, zScale);
    }
}
