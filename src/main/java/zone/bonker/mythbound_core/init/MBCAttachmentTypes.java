package zone.bonker.mythbound_core.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.data.EntityAbilities;

import java.util.function.Supplier;

public class MBCAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MythboundCore.MODID);

    public static final Supplier<AttachmentType<EntityAbilities>> ABILITIES =
            ATTACHMENT_TYPES.register("equipped_abilities", () -> AttachmentType.builder(EntityAbilities::new)
                    .serialize(EntityAbilities.CODEC)
                    .sync(EntityAbilities.NETWORK_CODEC)
                    .copyOnDeath()
                    .build());
}
