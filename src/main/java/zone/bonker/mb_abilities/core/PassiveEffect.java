package zone.bonker.mb_abilities.core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class PassiveEffect {
    private final Component name;
    @Nullable
    private List<Component> description;

    protected PassiveEffect(Component name) {
        this.name = name;
    }

    public abstract AbilityType<?> getType();

    protected abstract List<Component> makeDescription(RegistryAccess registryAccess);

    public void passiveTickServer(ServerLevel level, LivingEntity entity) {

    }

    public void passiveTickClient(Level level, LivingEntity entity) {

    }

    public void passiveEffectStartServer(ServerLevel level, LivingEntity entity) {

    }

    public void passiveEffectStartClient(Level level, LivingEntity entity) {

    }

    public void passiveEffectEndServer(ServerLevel level, LivingEntity entity) {

    }

    public void passiveEffectEndClient(Level level, LivingEntity entity) {

    }

    public final Component getName() {
        return name;
    }

    public final List<Component> getDescription(RegistryAccess registryAccess) {
        if (description == null) {
            description = makeDescription(registryAccess);
        }
        return description;
    }
}
