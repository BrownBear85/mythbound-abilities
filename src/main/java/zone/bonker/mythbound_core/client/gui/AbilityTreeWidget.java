package zone.bonker.mythbound_core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AbilityTreeWidget extends AbilityTreePanel {
    private final boolean mainAbilityTree;
    private final NamedAndDescribed owner;
    private final List<AbilityWidget> abilities = new ArrayList<>();
    private Supplier<Integer> pointGetter = () -> 0;

    public AbilityTreeWidget(NamedAndDescribed owner, AbilityTree abilityTree, boolean mainAbilityTree) {
        super(!mainAbilityTree);
        this.owner = owner;
        this.mainAbilityTree = mainAbilityTree;

        width = AbilityTreesScreen.WINDOW_INSIDE_WIDTH / 2;

        for (AbilityTree.Node node : abilityTree.nodes()) {
            AbilityWidget widget = new AbilityWidget(abilityTree, node, mainAbilityTree);
            abilities.add(widget);

            width = Math.max(width, widget.getX() + AbilityTreesScreen.WIDGET_WIDTH);
            height = Math.max(height, widget.getY() + AbilityTreesScreen.WIDGET_HEIGHT);
        }
    }

    @Override
    public boolean isInvalid(@Nullable NamedAndDescribed namedAndDescribed) {
        return namedAndDescribed != owner;
    }

    @Override
    public void drawInside(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        for (AbilityWidget widget : abilities) {
            widget.renderLines(guiGraphics, mouseX, mouseY);
        }

        for (AbilityWidget widget : abilities) {
            widget.renderSelf(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public void drawAbove(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.drawAbove(guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, owner.name(), getWidth() / 2, 4, 0xFFEDEDED);

        Font font = Minecraft.getInstance().font;
        String str = String.valueOf(pointGetter.get());
        int x = (getWidth() - font.width(str)) / 2;
        int y = 16;
        int borderColor = 0x000000;
        int textColor = 0xFFFFFF00;
        guiGraphics.drawString(font, str, x + 1, y, borderColor, false);
        guiGraphics.drawString(font, str, x - 1, y, borderColor, false);
        guiGraphics.drawString(font, str, x, y + 1, borderColor, false);
        guiGraphics.drawString(font, str, x, y - 1, borderColor, false);
        guiGraphics.drawString(font, str, x, y, textColor, false);
    }

    @Override
    public void refreshWidgets(CharacterBuild characterBuild) {
        pointGetter = mainAbilityTree ? characterBuild::getClassUnlockPoints : characterBuild::getSubclassUnlockPoints;

        for (AbilityWidget widget : abilities) {
            widget.refreshStatus(characterBuild, pointGetter);
        }
    }

    @Override
    public @Nullable AbilityWidget getWidget(double mouseX, double mouseY) {
        for (AbilityWidget widget : abilities) {
            if (widget.isHovered(mouseX - offset, mouseY)) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public List<Component> getTooltip(double mouseX, double mouseY) {
        AbilityWidget hoveredWidget = getWidget(mouseX, mouseY);
        return hoveredWidget == null ? null : hoveredWidget.getTooltip(mouseX, mouseY);
    }

    @Override
    public boolean onClick(int button, double mouseX, double mouseY) {
        AbilityWidget hoveredWidget = getWidget(mouseX, mouseY);
        return hoveredWidget != null && hoveredWidget.onClick(button, mouseX, mouseY);
    }
}
