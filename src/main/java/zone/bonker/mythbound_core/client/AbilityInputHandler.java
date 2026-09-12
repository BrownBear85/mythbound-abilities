package zone.bonker.mythbound_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityBinding;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.networking.C2SCastAbilityPacket;
import zone.bonker.mythbound_core.networking.C2SSetBindingPacket;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber
public class AbilityInputHandler {
    @Nullable
    public static ResourceLocation abilityToBind = null;
    @Nullable
    private static InputConstants.Key pressedKey = null;

    /**
     * Called from KeyboardHandlerMixin. Returning true will prevent the input from being processed by vanilla.
      */
    public static boolean keyPressed(int keyCode, int scanCode, int action) {
        return handleInput(keyCode == InputConstants.KEY_ESCAPE, InputConstants.getKey(keyCode, scanCode), action);
    }

    @SubscribeEvent
    public static void mouseClicked(InputEvent.MouseButton.Pre event) {
        if (handleInput(false, InputConstants.Type.MOUSE.getOrCreate(event.getButton()), event.getAction())) {
            event.setCanceled(true);
        }
    }

    private static boolean handleInput(boolean isEscape, InputConstants.Key key, int action) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            abilityToBind = null;
            pressedKey = null;
            return false;
        }

        if (abilityToBind == null && action == GLFW.GLFW_PRESS && Minecraft.getInstance().screen == null) {
            Optional<CharacterBuild> optional = CharacterBuild.getExisting(player);
            if (optional.isEmpty()) {
                return false;
            }

            for (Map.Entry<ResourceLocation, AbilityBinding> entry : optional.get().getAbilityBindings().entrySet()) {
                if (!AbilityInputHandler.matches(entry.getValue(), key)) {
                    continue;
                }

                Ability ability = MythboundCore.ABILITIES.getOrThrow(entry.getKey());
                if (!ability.cost().canCast(player)) {
                    return false;
                }

                LivingEntity target = null;
                if (!ability.targeting().isEmpty()) {
                    target = ClientSpellTargeting.getTargetForAbility(player, ability);
                    if (target == null && ability.targeting().targetRequired()) {
                        return false;
                    }
                }

                PacketDistributor.sendToServer(new C2SCastAbilityPacket(entry.getKey(), target == null ? -1 : target.getId()));
                return true;
            }
        }

        if (abilityToBind != null) {
            if (action == GLFW.GLFW_REPEAT) {
                return true;
            }

            if (action == GLFW.GLFW_PRESS) {
                if (isEscape) {
                    PacketDistributor.sendToServer(new C2SSetBindingPacket(abilityToBind, new AbilityBinding(-1, (byte) 0, false, false, false)));

                    abilityToBind = null;
                    pressedKey = null;
                } else {
                    pressedKey = key;
                }
                return true;
            }

            if (action == GLFW.GLFW_RELEASE) {
                if (key == pressedKey) {
                    AbilityBinding binding = new AbilityBinding(
                            key.getValue(),
                            (byte) key.getType().ordinal(),
                            Screen.hasShiftDown() && !KeyModifier.SHIFT.matches(key),
                            Screen.hasControlDown() && !KeyModifier.CONTROL.matches(key),
                            Screen.hasAltDown() && !KeyModifier.ALT.matches(key));

                    PacketDistributor.sendToServer(new C2SSetBindingPacket(abilityToBind, binding));

                    abilityToBind = null;
                }

                pressedKey = null;
                return false;
            }
        }

        return false;
    }

    public static boolean matches(AbilityBinding binding, InputConstants.Key key) {
        return InputConstants.Type.values()[binding.type()].getOrCreate(binding.key()).equals(key)
                && (!binding.shift() || Screen.hasShiftDown())
                && (!binding.control() || Screen.hasControlDown())
                && (!binding.alt() || Screen.hasAltDown());
    }

    public static Component getDisplayName(AbilityBinding binding) {
        Component component = InputConstants.Type.values()[binding.type()].getOrCreate(binding.key()).getDisplayName();
        if (binding.alt()) {
            component = Component.translatable("neoforge.controlsgui.alt", component);
        }
        if (binding.control()) {
            component = Component.translatable(MythboundCoreClient.onMac() ? "neoforge.controlsgui.control.mac" : "neoforge.controlsgui.control", component);
        }
        if (binding.shift()) {
            component = Component.translatable("neoforge.controlsgui.shift", component);
        }
        return component;
    }
}
