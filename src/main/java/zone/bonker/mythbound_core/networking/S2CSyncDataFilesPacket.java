package zone.bonker.mythbound_core.networking;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.MythboundSerialization;

import java.util.Map;

public record S2CSyncDataFilesPacket(Map<ResourceLocation, JsonElement> data) implements CustomPacketPayload {
    public static final Type<S2CSyncDataFilesPacket> TYPE = new Type<>(MythboundCore.identifier("s2c_sync_data_files"));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncDataFilesPacket> CODEC = StreamCodec.composite(
            MythboundSerialization.RESOURCE_LIST_CODEC, S2CSyncDataFilesPacket::data,
            S2CSyncDataFilesPacket::new);

    @Override
    public Type<S2CSyncDataFilesPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Exception e = MythboundCore.DATA_FILES.applyClient(data);
            if (e != null) {
                context.disconnect(Component.translatable("error.mythbound_core.data_file_sync_error", e.getMessage()));
            }
        });
    }
}
