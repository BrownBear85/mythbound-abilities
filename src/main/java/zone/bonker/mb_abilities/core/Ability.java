package zone.bonker.mb_abilities.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mb_abilities.init.Abilities;
import zone.bonker.mb_abilities.networking.S2CAbilityCastPacket;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Ability {
    @Nullable
    private ResourceLocation id;
    @Nullable
    private Component name;
    @Nullable
    private List<Component> description;

    public abstract AbilityType<?> getType();

    public abstract void castServer(ServerLevel level, LivingEntity caster);

    public void castClient(Level level, LivingEntity caster) {

    }

    protected boolean canCast(ServerLevel level, LivingEntity caster) {
        return true;
    }

    protected Component makeName(RegistryAccess registryAccess) {
        ResourceLocation id = getId(registryAccess);
        return Component.translatable("ability." + id.getNamespace() + "." + id.getPath() + ".name");
    }

    protected List<Component> makeDescription(RegistryAccess registryAccess) {
        ResourceLocation id = getId(registryAccess);
        return List.of(
                Component.translatable("ability." + id.getNamespace() + "." + id.getPath() + ".description")
                        .withStyle(ChatFormatting.GRAY)
        );
    }

    public final void tryCast(ServerLevel level, LivingEntity caster) {
        if (canCast(level, caster)) {
            castServer(level, caster);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster,
                    new S2CAbilityCastPacket(caster.getId(), Abilities.getKeyOrThrow(level.registryAccess(), this)));
        }
    }

    public final ResourceLocation getId(RegistryAccess registryAccess) {
        if (id == null) {
            id = Abilities.getKeyOrThrow(registryAccess, this);
        }
        return id;
    }

    public final Component getName(RegistryAccess registryAccess) {
        if (name == null) {
            name = makeName(registryAccess);
        }
        return name;
    }

    public final List<Component> getDescription(RegistryAccess registryAccess) {
        if (description == null) {
            description = makeDescription(registryAccess);
        }
        return description;
    }
}
