package zone.bonker.mythbound_core.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.bonker.mythbound_core.client.MythboundCoreClient;

import java.util.HashMap;
import java.util.Map;

public class ReloadableJsonRegistry<T> {
    public static final Logger LOGGER = LogManager.getLogger();

    private final String directory;
    private final Codec<T> codec;
    private final Codec<T> byNameCodec;
    private BiMap<ResourceLocation, T> serverData;
    private BiMap<ResourceLocation, T> clientData;

    public ReloadableJsonRegistry(String directory, Codec<T> codec) {
        this.directory = directory;
        this.codec = codec;
        this.byNameCodec = ResourceLocation.CODEC.flatXmap(id -> {
            T obj = getData().get(id);
            if (obj == null) {
                return DataResult.error(() -> "Unknown id in " + directory + ": " + id);
            } else {
                return DataResult.success(obj);
            }
        }, obj -> {
            ResourceLocation id = getData().inverse().get(obj);
            if (id == null) {
                return DataResult.error(() -> "Unregistered object in " + directory + ": " + obj);
            } else {
                return DataResult.success(id);
            }
        });
    }

    public String getDirectory() {
        return directory;
    }

    public BiMap<ResourceLocation, T> getData() {
        if (serverData != null) {
            return serverData;
        } else if (clientData != null) {
            return clientData;
        } else {
            throw new IllegalStateException("Tried to access the " + directory + " registry before it was loaded");
        }
    }

    public Codec<T> byNameCodec() {
        return byNameCodec;
    }

    public void applyServer(Map<ResourceLocation, JsonElement> resourceList, ReloadableServerResources serverResources) {
        serverData = HashBiMap.create(apply(resourceList, serverResources.getRegistryLookup()));
    }

    public void applyClient(Map<ResourceLocation, JsonElement> resourceList) {
        clientData = HashBiMap.create(apply(resourceList, MythboundCoreClient.getRegistryLookup()));
    }

    private Map<ResourceLocation, T> apply(Map<ResourceLocation, JsonElement> resourceList, HolderLookup.Provider registryLookup) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

        Map<ResourceLocation, T> data = new HashMap<>();
        for (ResourceLocation id : resourceList.keySet()) {
            try {
                codec.parse(ops, resourceList.get(id))
                        .resultOrPartial(errorMsg -> LOGGER.warn("Found an error in {}/{}:{} - {}", id.getNamespace(), directory, id.getPath(), errorMsg))
                        .ifPresent(obj -> data.put(id, obj));
            } catch (Exception e) {
                LOGGER.warn("An error occurred whilst decoding {}/{}:{} - {}", id.getNamespace(), directory, id.getPath(), e);
            }
        }
        return data;
    }
}
