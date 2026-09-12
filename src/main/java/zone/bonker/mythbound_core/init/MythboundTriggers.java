package zone.bonker.mythbound_core.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.trigger.KillEntityTrigger;
import zone.bonker.mythbound_core.core.trigger.MythboundTrigger;
import zone.bonker.mythbound_core.core.trigger.TickTrigger;

import java.util.function.Supplier;

public class MythboundTriggers {
    public static final ResourceKey<Registry<MythboundTrigger<?>>> KEY =
            ResourceKey.createRegistryKey(MythboundCore.identifier("mythbound_triggers"));

    public static final DeferredRegister<MythboundTrigger<?>> REGISTER =
            DeferredRegister.create(KEY, MythboundCore.MODID);

    public static final Registry<MythboundTrigger<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).create();

    public static final Codec<MythboundTrigger<?>> CODEC = REGISTRY.byNameCodec();

    public static final Supplier<TickTrigger> TICK =
            REGISTER.register("tick", TickTrigger::new);

    public static final Supplier<KillEntityTrigger> KILL_ENTITY =
            REGISTER.register("kill_entity", KillEntityTrigger::new);
}
