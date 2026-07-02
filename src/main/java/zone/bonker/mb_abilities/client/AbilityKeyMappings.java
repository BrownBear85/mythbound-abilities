package zone.bonker.mb_abilities.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.networking.C2SAbilityKeyPressPacket;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class AbilityKeyMappings {
    private static final String CATEGORY = "key.categories." + MythboundAbilities.MODID;

    public static final List<KeyMapping> ABILITIES = new ArrayList<>();

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        int[] defaultKeys = {InputConstants.KEY_Z, InputConstants.KEY_X, InputConstants.KEY_C, InputConstants.KEY_V};
        for (int i = 0; i < MythboundAbilities.ABILITY_COUNT; i++) {
            KeyMapping keyMapping = new KeyMapping("key." + MythboundAbilities.MODID + ".ability" + (i + 1),
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    i < defaultKeys.length ? defaultKeys[i] : -1,
                    CATEGORY);

            ABILITIES.add(keyMapping);
            event.register(keyMapping);
        }
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        for (int i = 0; i < ABILITIES.size(); i++) {
            if (ABILITIES.get(i).consumeClick()) {
                PacketDistributor.sendToServer(new C2SAbilityKeyPressPacket((byte) (i + 1)));
            }
        }
    }
}
