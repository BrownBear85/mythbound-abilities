package zone.bonker.mythbound_core.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.bonker.mythbound_core.client.ExtendedPlayerModel;
import zone.bonker.mythbound_core.client.MythboundCoreClient;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/model/geom/ModelPart;Z)Lnet/minecraft/client/model/PlayerModel;"))
    private static <T extends LivingEntity> PlayerModel<T> redirect(ModelPart root, boolean slim) {
        return MythboundCoreClient.CURRENT_MODEL_PROPERTIES == null
                ? new PlayerModel<>(root, slim)
                : new ExtendedPlayerModel<>(slim, MythboundCoreClient.CURRENT_MODEL_PROPERTIES);
    }
}
