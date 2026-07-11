package zone.bonker.mythbound_core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import zone.bonker.mythbound_core.networking.S2CSyncRegistriesPacket;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MythboundReloadableRegistries extends SimplePreparableReloadListener<Map<String, Map<ResourceLocation, JsonElement>>> {
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    private final Map<String, ReloadableJsonRegistry<?>> registries = new LinkedHashMap<>();
    private ReloadableServerResources serverResources;
    private Map<String, Map<ResourceLocation, JsonElement>> lastData;

    public void addRegistry(ReloadableJsonRegistry<?> registry) {
        registries.put(registry.getDirectory(), registry);
    }

    public void setServerResources(ReloadableServerResources serverResources) {
        this.serverResources = serverResources;
        this.registries.clear();
    }

    public ReloadableJsonRegistry<?> getRegistry(String directory) {
        return registries.get(directory);
    }

    @Override
    protected Map<String, Map<ResourceLocation, JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, Map<ResourceLocation, JsonElement>> data = new HashMap<>();

        for (String directory : registries.keySet()) {
            Map<ResourceLocation, JsonElement> resourceList = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager, directory, GSON, resourceList);
            data.put(directory, resourceList);
        }

        return data;
    }

    @Override
    protected void apply(Map<String, Map<ResourceLocation, JsonElement>> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        for (Map.Entry<String, ReloadableJsonRegistry<?>> entry : registries.entrySet()) {
            entry.getValue().applyServer(data.get(entry.getKey()), serverResources);
        }

        this.lastData = data;

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && ServerLifecycleHooks.getCurrentServer() != null) {
            syncToPlayer(null);
        }
    }

    @Nullable
    public Exception applyClient(Map<String, Map<ResourceLocation, JsonElement>> data) {
        try {
            for (Map.Entry<String, ReloadableJsonRegistry<?>> entry : registries.entrySet()) {
                entry.getValue().applyClient(data.get(entry.getKey()));
            }
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public void syncToPlayer(@Nullable ServerPlayer player) {
        S2CSyncRegistriesPacket packet = new S2CSyncRegistriesPacket(lastData);
        if (player == null) {
            PacketDistributor.sendToAllPlayers(packet);
        } else {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}
