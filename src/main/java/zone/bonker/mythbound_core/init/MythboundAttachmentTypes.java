package zone.bonker.mythbound_core.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.data.OwnedAttachment;

import java.util.function.Supplier;

public class MythboundAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MythboundCore.MODID);

    public static final Supplier<AttachmentType<CharacterBuild>> CHARACTER_BUILD =
            ATTACHMENT_TYPES.register("character_build", () -> AttachmentType.builder(CharacterBuild::new)
                    .serialize(new OwnedAttachment.Serializer<>(CharacterBuild.CODEC))
                    .sync(new OwnedAttachment.SyncHandler<>(CharacterBuild.NETWORK_CODEC))
                    .copyOnDeath()
                    .build());
}
