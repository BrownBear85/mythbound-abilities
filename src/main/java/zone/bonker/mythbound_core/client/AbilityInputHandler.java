package zone.bonker.mythbound_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.AbilityBinding;
import zone.bonker.mythbound_core.data.AbilityReloadListener;
import zone.bonker.mythbound_core.data.EntityAbilities;
import zone.bonker.mythbound_core.networking.C2SCastAbilityPacket;
import zone.bonker.mythbound_core.networking.C2SSetBindingPacket;

import javax.annotation.Nullable;

@EventBusSubscriber(value = Dist.CLIENT)
public class AbilityInputHandler {
    @Nullable
    public static ResourceLocation abilityToBind = null;
    @Nullable
    private static InputConstants.Key pressedKey = null;

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (abilityToBind == null && event.getAction() == GLFW.GLFW_PRESS) {
            ResourceLocation id = EntityAbilities.getMatchingBoundAbility(player, event.getKey(), event.getScanCode());
            if (id != null) {
                Ability ability = AbilityReloadListener.getData().get(id);
                if (ability != null && ability.canCast(player.level(), player)) {
                    PacketDistributor.sendToServer(new C2SCastAbilityPacket(id));
                }
            }
        }

        if (abilityToBind != null) {
            Ability ability = AbilityReloadListener.getData().get(abilityToBind);
            if (ability == null) {
                abilityToBind = null;
                return;
            }

            InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());

            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                    PacketDistributor.sendToServer(new C2SSetBindingPacket(abilityToBind, new AbilityBinding(-1, (byte) 0, false, false, false)));

                    player.sendSystemMessage(
                            Component.translatable("commands." + MythboundCore.MODID + ".unbound_ability",
                                    ability.getName()));

                    abilityToBind = null;
                    pressedKey = null;
                } else {
                    pressedKey = key;
                }
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                if (key == pressedKey) {
                    AbilityBinding binding = new AbilityBinding(key.getValue(), (byte) key.getType().ordinal(), Screen.hasShiftDown(), Screen.hasControlDown(), Screen.hasAltDown());

                    PacketDistributor.sendToServer(new C2SSetBindingPacket(abilityToBind, binding));

                    player.sendSystemMessage(
                            Component.translatable("commands." + MythboundCore.MODID + ".bound_ability",
                                    ability.getName(),
                                    MythboundCoreClient.getDisplayName(binding)));

                    abilityToBind = null;
                }

                pressedKey = null;
            }
        }
    }

    public static boolean matches(AbilityBinding binding, int keyCode, int scanCode) {
        return InputConstants.Type.values()[binding.type()].getOrCreate(binding.key()).equals(InputConstants.getKey(keyCode, scanCode))
                && (!binding.shift() || Screen.hasShiftDown())
                && (!binding.control() || Screen.hasControlDown())
                && (!binding.alt() || Screen.hasAltDown());
    }
}
