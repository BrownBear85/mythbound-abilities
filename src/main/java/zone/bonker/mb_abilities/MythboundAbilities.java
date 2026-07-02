package zone.bonker.mb_abilities;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import zone.bonker.mb_abilities.init.Abilities;
import zone.bonker.mb_abilities.init.AbilityTypes;
import zone.bonker.mb_abilities.init.AbilityAttachmentTypes;

@Mod(MythboundAbilities.MODID)
public class MythboundAbilities {
    public static final int ABILITY_COUNT = 10;

    public static final String MODID = "mb_abilities";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MythboundAbilities(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::newRegistries);
        modEventBus.addListener(this::newDatapackRegistries);

        AbilityTypes.ABILITY_TYPES.register(modEventBus);
        AbilityAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //// EVENTS

    private void newRegistries(NewRegistryEvent event) {
        event.register(AbilityTypes.REGISTRY);
    }

    private void newDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Abilities.ABILITIES_KEY, Abilities.DIRECT_CODEC, Abilities.DIRECT_CODEC, builder -> builder.sync(true));
    }
}
