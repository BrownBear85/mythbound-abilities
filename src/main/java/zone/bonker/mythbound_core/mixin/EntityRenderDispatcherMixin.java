package zone.bonker.mythbound_core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zone.bonker.mythbound_core.client.MythboundCoreClient;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @ModifyReturnValue(method = "getRenderer", at = @At("RETURN"))
    public <T extends Entity> EntityRenderer<? super T> getRenderer(EntityRenderer<T> original, T entity) {
        return MythboundCoreClient.replaceEntityRenderer(entity, original);
    }
}
