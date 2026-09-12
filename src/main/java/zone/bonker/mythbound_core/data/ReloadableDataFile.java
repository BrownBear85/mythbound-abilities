package zone.bonker.mythbound_core.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;

public class ReloadableDataFile<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation fileId;
    private final Codec<T> codec;
    @Nullable
    private T serverData;
    @Nullable
    private T clientData;

    public ReloadableDataFile(ResourceLocation fileId, Codec<T> codec) {
        this.fileId = fileId;
        this.codec = codec;
    }

    public ResourceLocation getFileId() {
        return fileId;
    }

    public T get() {
        if (serverData != null) {
            return serverData;
        } else if (clientData != null) {
            return clientData;
        } else {
            throw new NullPointerException("Tried to access data file " + fileId + " before it was loaded");
        }
    }

    public void applyServer(JsonElement jsonElement, ReloadableServerResources serverResources) {
        serverData = apply(jsonElement, serverResources.getRegistryLookup());
    }

    public void applyClient(JsonElement jsonElement, ReloadableServerResources serverResources) {
        clientData = apply(jsonElement, serverResources.getRegistryLookup());
    }

    @Nullable
    private T apply(JsonElement jsonElement, HolderLookup.Provider registryLookup) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

        try {
            Optional<T> optional = codec.parse(ops, jsonElement)
                    .resultOrPartial(errorMsg -> LOGGER.warn("Found an error in data file {} - {}", fileId, errorMsg));
            if (optional.isPresent()) {
                return optional.get();
            }
        } catch (Exception e) {
            LOGGER.warn("An error occurred whilst decoding data file {} - {}", fileId, e);
        }

        return null;
    }
}
