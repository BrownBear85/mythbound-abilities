package zone.bonker.mythbound_core.client.gui.screen.ability_tree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zone.bonker.mythbound_core.core.NamedAndDescribed;

public class EmptyPanel extends AbilityTreePanel {
    public EmptyPanel(boolean rightMostPanel) {
        super(rightMostPanel);

        width = AbilityTreesScreen.WINDOW_INSIDE_WIDTH / 2;
        height = AbilityTreesScreen.WINDOW_INSIDE_HEIGHT;
    }

    @Override
    public boolean isInvalid(@Nullable NamedAndDescribed namedAndDescribed) {
        return namedAndDescribed != null;
    }

    @Override
    public void drawAbove(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.drawAbove(guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("gui.mythbound_core.ability_tree.no_subclass"), offset + width / 2, height / 2, 0xFFFFFFFF);
    }
}
