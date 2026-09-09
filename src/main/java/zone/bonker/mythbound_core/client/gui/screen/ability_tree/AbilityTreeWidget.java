package zone.bonker.mythbound_core.client.gui.screen.ability_tree;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zone.bonker.mythbound_core.client.MythboundRendering;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.ArrayList;
import java.util.List;

public class AbilityTreeWidget extends AbilityTreePanel {
    private final boolean mainAbilityTree;
    private final NamedAndDescribed owner;
    private final List<AbilityWidget> abilities = new ArrayList<>();
    @Nullable
    private CharacterBuild characterBuild;

    public AbilityTreeWidget(NamedAndDescribed owner, AbilityTree abilityTree, boolean mainAbilityTree) {
        super(!mainAbilityTree);
        this.owner = owner;
        this.mainAbilityTree = mainAbilityTree;

        width = AbilityTreesScreen.WINDOW_INSIDE_WIDTH / 2;

        for (AbilityTree.Node node : abilityTree.nodes()) {
            AbilityWidget widget = new AbilityWidget(abilityTree, node, mainAbilityTree);
            abilities.add(widget);

            width = Math.max(width, widget.getX() + AbilityWidget.SIZE);
            height = Math.max(height, widget.getY() + AbilityWidget.SIZE);
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

        // Title
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, owner.name(), getWidth() / 2, 4, 0xFFEDEDED);

        if (characterBuild != null) {
            // Point count
            int points = mainAbilityTree ? characterBuild.getClassUnlockPoints() : characterBuild.getSubclassUnlockPoints();
            MythboundRendering.drawCenteredOutlinedString(guiGraphics, Minecraft.getInstance().font,
                    String.valueOf(points), getWidth() / 2, 16, 0xFFFFFF00, 0x000000);
        }
    }

    @Override
    public void refreshWidgets(CharacterBuild characterBuild) {
        this.characterBuild = characterBuild;

        for (AbilityWidget widget : abilities) {
            widget.refreshStatus(characterBuild);
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
