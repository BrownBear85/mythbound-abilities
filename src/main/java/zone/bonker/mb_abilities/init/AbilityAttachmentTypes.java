package zone.bonker.mb_abilities.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import zone.bonker.mb_abilities.MythboundAbilities;
import zone.bonker.mb_abilities.data.EntityAbilities;

import java.util.function.Supplier;

public class AbilityAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MythboundAbilities.MODID);

    public static final Supplier<AttachmentType<EntityAbilities>> ABILITIES =
            ATTACHMENT_TYPES.register("equipped_abilities", () -> AttachmentType.builder(EntityAbilities::new)
                    .serialize(EntityAbilities.CODEC)
                    .sync(EntityAbilities.NETWORK_CODEC)
                    .copyOnDeath()
                    .build());
}
