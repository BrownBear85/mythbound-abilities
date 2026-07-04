package zone.bonker.mythbound_core;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import zone.bonker.mythbound_core.data.AbilityReloadListener;
import zone.bonker.mythbound_core.init.AbilityEffectSerializers;
import zone.bonker.mythbound_core.init.AbilitySerializers;
import zone.bonker.mythbound_core.init.MBCAttachmentTypes;
import zone.bonker.mythbound_core.networking.S2CSyncAbilitiesPacket;
import zone.bonker.mythbound_core.server.MBCCommands;

@Mod(MythboundCore.MODID)
public class MythboundCore {
    public static final String MODID = "mythbound_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MythboundCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::newRegistries);

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::playerLoggingIn);

        AbilitySerializers.REGISTER.register(modEventBus);
        AbilityEffectSerializers.REGISTER.register(modEventBus);

        MBCAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        MBCCommands.ARGUMENT_TYPE_INFOS.register(modEventBus);
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //// EVENTS

    private void newRegistries(NewRegistryEvent event) {
        event.register(AbilitySerializers.REGISTRY);
        event.register(AbilityEffectSerializers.REGISTRY);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(AbilityReloadListener.create(event.getServerResources()));
    }

    private void playerLoggingIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            PacketDistributor.sendToPlayer(serverPlayer, new S2CSyncAbilitiesPacket(AbilityReloadListener.getResourceList(), false));
        }
    }
}
