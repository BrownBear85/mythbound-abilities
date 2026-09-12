package zone.bonker.mythbound_core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.networking.S2CSyncDataFilesPacket;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MythboundReloadableDataFiles extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    private final Map<ResourceLocation, ReloadableDataFile<?>> files = new LinkedHashMap<>();
    private final ReloadableServerResources serverResources;
    private Map<ResourceLocation, JsonElement> lastData;

    public MythboundReloadableDataFiles(ReloadableServerResources serverResources) {
        this.serverResources = serverResources;
    }

    public void addFile(ReloadableDataFile<?> dataFile) {
        files.put(dataFile.getFileId(), dataFile);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> data = new HashMap<>();

        for (ResourceLocation fileId : files.keySet()) {
            FileToIdConverter fileToIdConverter = FileToIdConverter.json("");
            ResourceLocation filePath = fileToIdConverter.idToFile(fileId);

            Optional<Resource> resource = resourceManager.getResource(filePath);
            if (resource.isEmpty()) {
                MythboundCore.LOGGER.error("Couldn't find any data for reloadable file {}", fileId);
                continue;
            }

            try (Reader reader = resource.get().openAsReader()) {
                data.put(fileId, GsonHelper.fromJson(GSON, reader, JsonElement.class));
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                MythboundCore.LOGGER.error("Couldn't parse data file {} from {}", filePath, fileId, e);
            }
        }

        return data;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        for (Map.Entry<ResourceLocation, ReloadableDataFile<?>> entry : files.entrySet()) {
            entry.getValue().applyServer(data.get(entry.getKey()), serverResources);
        }

        this.lastData = data;

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && ServerLifecycleHooks.getCurrentServer() != null) {
            syncToPlayer(null);
        }
    }

    @Nullable
    public Exception applyClient(Map<ResourceLocation, JsonElement> data) {
        try {
            for (Map.Entry<ResourceLocation, ReloadableDataFile<?>> entry : files.entrySet()) {
                entry.getValue().applyClient(data.get(entry.getKey()), serverResources);
            }
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public void syncToPlayer(@Nullable ServerPlayer player) {
        S2CSyncDataFilesPacket packet = new S2CSyncDataFilesPacket(lastData);
        if (player == null) {
            PacketDistributor.sendToAllPlayers(packet);
        } else {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}