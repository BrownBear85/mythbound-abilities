package zone.bonker.mythbound_core.networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.CharacterModelExtensions;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.MythboundReloadableRegistries;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncRegistriesPacket(Map<String, Map<ResourceLocation, JsonElement>> data) implements CustomPacketPayload {
    public static final Type<S2CSyncRegistriesPacket> TYPE = new Type<>(MythboundCore.identifier("s2c_sync_registries"));

    private static final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, JsonElement>> RESOURCE_LIST_CODEC =
            ByteBufCodecs.map(HashMap::new,
                    ResourceLocation.STREAM_CODEC,
                    ByteBufCodecs.STRING_UTF8.map(JsonParser::parseString, MythboundReloadableRegistries.GSON::toJson));

    private static final StreamCodec<FriendlyByteBuf, Map<String, Map<ResourceLocation, JsonElement>>> DATA_CODEC =
            ByteBufCodecs.map(HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    RESOURCE_LIST_CODEC);

    public static final StreamCodec<FriendlyByteBuf, S2CSyncRegistriesPacket> CODEC = StreamCodec.composite(
            DATA_CODEC, S2CSyncRegistriesPacket::data,
            S2CSyncRegistriesPacket::new);

    @Override
    public Type<S2CSyncRegistriesPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Exception e = MythboundCore.REGISTRIES.applyClient(data);
            if (e != null) {
                context.disconnect(Component.literal("An error occurred whilst syncing Mythbound's reloadable datapack registries: " + e.getMessage()));
            }
        });
    }
}
