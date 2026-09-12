package zone.bonker.mythbound_core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public abstract class OwnedAttachment {
    @Nullable
    private LivingEntity owner;

    public OwnedAttachment() {

    }

    public OwnedAttachment(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException(getClass().getCanonicalName() + " can only be attached to LivingEntities");
        }
        setOwner(livingEntity);
    }

    protected LivingEntity owner() {
        if (owner == null) {
            throw new NullPointerException("owner is null");
        }
        return owner;
    }

    void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    protected void onUpdatedOnClient() {

    }

    public record Serializer<T extends OwnedAttachment>(Codec<T> codec) implements IAttachmentSerializer<Tag, T> {
        @Override
        public @NotNull T read(@NotNull IAttachmentHolder holder, @NotNull Tag tag, HolderLookup.Provider provider) {
            final DataResult<T> parsingResult = codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag);
            T obj = parsingResult.getOrThrow(msg -> buildException("read", msg));
            obj.setOwner((LivingEntity) holder);
            return obj;
        }

        @Nullable
        @Override
        public Tag write(@NotNull T attachment, HolderLookup.Provider provider) {
            final DataResult<Tag> encodingResult = codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment);
            return encodingResult.getOrThrow(msg -> buildException("write", msg));
        }

        private RuntimeException buildException(final String operation, final String error) {
            return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error: " + error);
        }
    }

    public record SyncHandler<T extends OwnedAttachment>(StreamCodec<RegistryFriendlyByteBuf, T> networkCodec,
                                                         BiPredicate<IAttachmentHolder, ServerPlayer> sendPredicate)
            implements AttachmentSyncHandler<T> {

        public SyncHandler(StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {
            this(networkCodec, (h, p) -> true);
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync) {
            networkCodec.encode(buf, attachment);
        }

        @Override
        public T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
            T obj = networkCodec.decode(buf);
            obj.setOwner((LivingEntity) holder);
            obj.onUpdatedOnClient();
            return obj;
        }

        @Override
        public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
            return sendPredicate.test(holder, to);
        }
    }
}
