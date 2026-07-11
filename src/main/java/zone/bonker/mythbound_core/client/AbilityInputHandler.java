package zone.bonker.mythbound_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AbilityBinding;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.networking.C2SCastAbilityPacket;
import zone.bonker.mythbound_core.networking.C2SSetBindingPacket;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class AbilityInputHandler {
    @Nullable
    public static ResourceLocation abilityToBind = null;
    @Nullable
    private static InputConstants.Key pressedKey = null;

    public static boolean keyPressed(int keyCode, int scanCode, int action) {
        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().screen != null) {
            abilityToBind = null;
            pressedKey = null;
            return false;
        }

        if (abilityToBind == null && action == GLFW.GLFW_PRESS) {
            Optional<CharacterBuild> optional = CharacterBuild.getExisting(player);
            if (optional.isEmpty()) {
                return false;
            }

            for (Iterator<Map.Entry<ResourceLocation, AbilityBinding>> iterator = optional.get().getAbilityBindings().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<ResourceLocation, AbilityBinding> entry = iterator.next();
                if (AbilityInputHandler.matches(entry.getValue(), keyCode, scanCode) && optional.get().hasAbility(entry.getKey())) {
                    Ability ability = MythboundCore.ABILITIES.getData().get(entry.getKey());
                    if (ability == null) {
                        MythboundCoreClient.LOGGER.warn("{} was bound to an unregistered ability id {}, unbinding",
                                getDisplayName(entry.getValue()).getString(), entry.getKey());
                        iterator.remove();
                    } else {
                        PacketDistributor.sendToServer(new C2SCastAbilityPacket(entry.getKey()));
                    }
                    return false;
                }
            }
        }

        if (abilityToBind != null) {
            if (action == GLFW.GLFW_REPEAT) {
                return true;
            }

            Ability ability = MythboundCore.ABILITIES.getData().get(abilityToBind);
            if (ability == null) {
                abilityToBind = null;
                pressedKey = null;
                return false;
            }

            InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);

            if (action == GLFW.GLFW_PRESS) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    PacketDistributor.sendToServer(new C2SSetBindingPacket(abilityToBind, new AbilityBinding(-1, (byte) 0, false, false, false)));

                    player.sendSystemMessage(
                            Component.translatable("commands." + MythboundCore.MODID + ".unbound_ability",
                                    ability.name()));

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

                    player.sendSystemMessage(
                            Component.translatable("commands." + MythboundCore.MODID + ".bound_ability",
                                    ability.name(),
                                    getDisplayName(binding)));

                    abilityToBind = null;
                }

                pressedKey = null;
                return false;
            }
        }

        return false;
    }

    public static boolean matches(AbilityBinding binding, int keyCode, int scanCode) {
        return InputConstants.Type.values()[binding.type()].getOrCreate(binding.key()).equals(InputConstants.getKey(keyCode, scanCode))
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
