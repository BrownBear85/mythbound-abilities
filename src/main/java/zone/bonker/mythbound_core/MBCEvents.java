package zone.bonker.mythbound_core;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import zone.bonker.mythbound_core.data.EntityAbilities;

@EventBusSubscriber
public class MBCEvents {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            EntityAbilities.tickUnlockedAbilities(livingEntity);
        }
    }
}
