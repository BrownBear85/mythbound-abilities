package zone.bonker.mythbound_core.networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.AbilityReloadListener;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncAbilitiesPacket(Map<ResourceLocation, JsonElement> resourceList, boolean isConfiguration) implements CustomPacketPayload {
    public static final Type<S2CSyncAbilitiesPacket> TYPE = new Type<>(MythboundCore.identifier("s2c_sync_abilities"));

    private static final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, JsonElement>> RESOURCE_LIST_CODEC =
            ByteBufCodecs.map(HashMap::new,
                    ResourceLocation.STREAM_CODEC,
                    ByteBufCodecs.STRING_UTF8.map(JsonParser::parseString, AbilityReloadListener.GSON::toJson));

    public static final StreamCodec<FriendlyByteBuf, S2CSyncAbilitiesPacket> CODEC = StreamCodec.composite(
            RESOURCE_LIST_CODEC, S2CSyncAbilitiesPacket::resourceList,
            ByteBufCodecs.BOOL, S2CSyncAbilitiesPacket::isConfiguration,
            S2CSyncAbilitiesPacket::new);

    @Override
    public Type<S2CSyncAbilitiesPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> AbilityReloadListener.setClientData(resourceList));
    }
}
