package zone.bonker.mythbound_core.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.bonker.mythbound_core.client.AbilityInputHandler;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(JI)Z",
                    ordinal = 0),
            cancellable = true)
    public void keyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (AbilityInputHandler.keyPressed(key, scanCode, action)) {
            ci.cancel();
        }
    }
}
