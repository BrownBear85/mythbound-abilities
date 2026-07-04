package zone.bonker.mythbound_core.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import zone.bonker.mythbound_core.client.MythboundCoreClient;
import zone.bonker.mythbound_core.core.Ability;
import zone.bonker.mythbound_core.networking.S2CSyncAbilitiesPacket;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class AbilityReloadListener extends SimpleJsonResourceReloadListener {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new Gson();

    @Nullable
    private static AbilityReloadListener SERVER_INSTANCE;
    private static final BiMap<ResourceLocation, Ability> CLIENT_DATA = HashBiMap.create();

    private final ReloadableServerResources serverResources;
    private final BiMap<ResourceLocation, Ability> data = HashBiMap.create();
    private final Map<ResourceLocation, JsonElement> resourceList = new HashMap<>();

    private AbilityReloadListener(ReloadableServerResources serverResources) {
        super(GSON, "abilities");
        this.serverResources = serverResources;
    }

    public static AbilityReloadListener create(ReloadableServerResources resources) {
        SERVER_INSTANCE = new AbilityReloadListener(resources);
        return SERVER_INSTANCE;
    }

    public static BiMap<ResourceLocation, Ability> getData() {
        return SERVER_INSTANCE == null ? CLIENT_DATA : SERVER_INSTANCE.data;
    }

    public static Map<ResourceLocation, JsonElement> getResourceList() {
        return Objects.requireNonNull(SERVER_INSTANCE).resourceList;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resourceList,
                         @NotNull ResourceManager resourceManagerIn,
                         @NotNull ProfilerFiller profilerIn) {
        LOGGER.info("Applying ability reload listener on thread {}", Thread.currentThread().getName());

        data.clear();
        this.resourceList.clear();

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, serverResources.getRegistryLookup());

        resourceList = new HashMap<>(resourceList);
        for (Iterator<ResourceLocation> iterator = resourceList.keySet().iterator(); iterator.hasNext(); ) {
            ResourceLocation id = iterator.next();
            try {
                Ability.DIRECT_CODEC.parse(ops, resourceList.get(id))
                        .resultOrPartial(errorMsg -> {
                            LOGGER.error("Could not decode ability with id {} - {}", id, errorMsg);
                            iterator.remove();
                        })
                        .ifPresent(obj -> data.put(id, obj));
            } catch (Exception e) {
                LOGGER.error("An error occurred while loading ability with id {}", id, e);
                iterator.remove();
            }
        }

        CLIENT_DATA.putAll(data);
        this.resourceList.putAll(resourceList);

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && ServerLifecycleHooks.getCurrentServer() != null) {
            PacketDistributor.sendToAllPlayers(new S2CSyncAbilitiesPacket(resourceList, false));
        }
    }

    public static void setClientData(Map<ResourceLocation, JsonElement> resourceList) {
        CLIENT_DATA.clear();

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, MythboundCoreClient.getRegistryLookup());

        for (ResourceLocation id : resourceList.keySet()) {
            try {
                Ability.DIRECT_CODEC.parse(ops, resourceList.get(id))
                        .resultOrPartial(errorMsg -> {
                            LOGGER.warn("Could not decode ability with id {} on the client, skipping - {}", id, errorMsg);
                        })
                        .ifPresent(obj -> CLIENT_DATA.put(id, obj));
            } catch (Exception e) {
                LOGGER.warn("An error occurred while loading ability with id {} on the client, skipping", id, e);
            }
        }
    }
}
