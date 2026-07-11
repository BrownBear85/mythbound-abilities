package zone.bonker.mythbound_core.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class MythboundNetworking {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Server to Client

        registrar.playToClient(
                S2CEntityAbilityPacket.TYPE,
                S2CEntityAbilityPacket.CODEC,
                S2CEntityAbilityPacket::handle);

        registrar.playToClient(
                S2CSyncRegistriesPacket.TYPE,
                S2CSyncRegistriesPacket.CODEC,
                S2CSyncRegistriesPacket::handle);

        // Client to Server

        registrar.playToServer(
                C2SCastAbilityPacket.TYPE,
                C2SCastAbilityPacket.CODEC,
                C2SCastAbilityPacket::handle);

        registrar.playToServer(
                C2SSetBindingPacket.TYPE,
                C2SSetBindingPacket.CODEC,
                C2SSetBindingPacket::handle);
    }
}
