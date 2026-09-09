package zone.bonker.mythbound_core.client.gui.screen.ability_tree;

import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public interface HoverableClickable {
    @Nullable
    default List<Component> getTooltip(double mouseX, double mouseY) {
        return null;
    }

    default boolean onClick(int button, double mouseX, double mouseY) {
        return false;
    }
}
