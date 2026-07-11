package zone.bonker.mythbound_core.core;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface NamedAndDescribed {
    Component name();

    List<Component> description();
}
