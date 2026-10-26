package zone.bonker.mythbound_core.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.trigger.SpellTrigger;
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

    // Spell Casting

    public static final Supplier<SpellTrigger> SPELL_WINDUP_START =
            REGISTER.register("spell_windup_start", SpellTrigger::new);

    public static final Supplier<SpellTrigger> SPELL_WINDUP_TICK =
            REGISTER.register("spell_windup_tick", SpellTrigger::new);

    public static final Supplier<SpellTrigger> SPELL_WINDUP_RELEASE =
            REGISTER.register("spell_windup_release", SpellTrigger::new);

    public static final Supplier<SpellTrigger> SPELL_CAST =
            REGISTER.register("spell_cast", SpellTrigger::new);

    public static final Supplier<SpellTrigger> SPELL_HOLD_TICK =
            REGISTER.register("spell_hold_tick", SpellTrigger::new);

    public static final Supplier<SpellTrigger> SPELL_HOLD_END =
            REGISTER.register("spell_hold_end", SpellTrigger::new);
}
