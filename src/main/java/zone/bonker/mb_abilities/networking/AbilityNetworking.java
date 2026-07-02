package zone.bonker.mb_abilities.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class AbilityNetworking {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Server to Client

        registrar.playToClient(
                S2CAbilityCastPacket.TYPE,
                S2CAbilityCastPacket.CODEC,
                S2CAbilityCastPacket::handle);

        // Client to Client

        registrar.playToServer(
                C2SAbilityKeyPressPacket.TYPE,
                C2SAbilityKeyPressPacket.CODEC,
                C2SAbilityKeyPressPacket::handle);
    }
}
