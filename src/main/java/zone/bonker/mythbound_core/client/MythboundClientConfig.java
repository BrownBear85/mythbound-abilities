package zone.bonker.mythbound_core.client;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MythboundClientConfig {
    public static final MythboundClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<MythboundClientConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(MythboundClientConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue displaySpellBinds;

    public MythboundClientConfig(ModConfigSpec.Builder builder) {
        displaySpellBinds = builder.comment("Whether the spell overlay should display each spell's keybind")
                .define("display_spell_binds", true);
    }
}
