package zone.bonker.mythbound_core;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Race;
import zone.bonker.mythbound_core.data.MythboundReloadableRegistries;
import zone.bonker.mythbound_core.data.ReloadableJsonRegistry;
import zone.bonker.mythbound_core.init.MythboundEffectSerializers;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;
import zone.bonker.mythbound_core.server.MythboundCommands;

@Mod(MythboundCore.MODID)
public class MythboundCore {
    public static final String MODID = "mythbound_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final MythboundReloadableRegistries REGISTRIES = new MythboundReloadableRegistries();
    public static final ReloadableJsonRegistry<CharacterClass> CLASSES = new ReloadableJsonRegistry<>("classes", CharacterClass.CODEC);
    public static final ReloadableJsonRegistry<Race> RACES = new ReloadableJsonRegistry<>("races", Race.CODEC);
    public static final ReloadableJsonRegistry<Ability> ABILITIES = new ReloadableJsonRegistry<>("abilities", Ability.CODEC);

    public MythboundCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::newRegistries);

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);

        MythboundEffectSerializers.REGISTER.register(modEventBus);

        MythboundAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        MythboundCommands.ARGUMENT_TYPE_INFOS.register(modEventBus);
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //// EVENTS

    private void newRegistries(NewRegistryEvent event) {
        event.register(MythboundEffectSerializers.REGISTRY);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        REGISTRIES.setServerResources(event.getServerResources());
        REGISTRIES.addRegistry(ABILITIES); // Load abilities first since they don't need to know about classes or races.
        REGISTRIES.addRegistry(CLASSES); // Load classes second since they need to know about abilities but usually don't need races.
        REGISTRIES.addRegistry(RACES); // Load races last since they definitely need both classes and abilities to be loaded.
        event.addListener(REGISTRIES);
    }
}
