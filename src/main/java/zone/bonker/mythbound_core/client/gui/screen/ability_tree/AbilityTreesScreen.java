package zone.bonker.mythbound_core.client.gui.screen.ability_tree;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.CharacterClass;
import zone.bonker.mythbound_core.core.Subclass;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.List;

public class AbilityTreesScreen extends Screen implements RefreshWithCharacterBuild {
    public static final int WINDOW_WIDTH = 338;
    public static final int WINDOW_HEIGHT = 227;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH - 2 * WINDOW_INSIDE_X;
    public static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT - WINDOW_INSIDE_X - WINDOW_INSIDE_Y;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;

    private static final double SCROLL_SPEED = 16.0;

    public static final ResourceLocation TEXTURE = MythboundCore.identifier("textures/gui/ability_tree_window.png");

    private AbilityTreePanel mainAbilityTree;
    private AbilityTreePanel subclassAbilityTree;
    private boolean isScrolling = false;
    private int left, top;
    private int maxX, maxY;
    private double panX, panY;

    public AbilityTreesScreen(CharacterBuild characterBuild) {
        super(Component.translatable("gui.mythbound_core.ability_tree.title"));
        refreshWidgets(characterBuild);
    }

    public void refreshWidgets(CharacterBuild characterBuild) {
        CharacterClass characterClass = characterBuild.getCharacterClass();
        if (characterClass == null) {
            Minecraft.getInstance().popGuiLayer();
            return;
        }

        if (mainAbilityTree == null || mainAbilityTree.isInvalid(characterClass)) {
            mainAbilityTree = new AbilityTreeWidget(characterClass, characterClass.mainAbilityTree(), true);
        }

        Subclass subclass = characterBuild.getSubclass();
        if (subclassAbilityTree == null || subclassAbilityTree.isInvalid(subclass)) {
            subclassAbilityTree = subclass == null
                    ? new EmptyPanel(true)
                    : new AbilityTreeWidget(subclass, subclass.abilityTree(), false);
        }

        mainAbilityTree.refreshWidgets(characterBuild);
        subclassAbilityTree.refreshWidgets(characterBuild);
    }

    @Override
    protected void init() {
        super.init();

        left = (this.width - WINDOW_WIDTH) / 2;
        top = (this.height - WINDOW_HEIGHT) / 2;

        maxX = 0;
        maxY = 0;

        mainAbilityTree.init(maxX);
        maxX += mainAbilityTree.getWidth();
        maxY = Math.max(maxY, mainAbilityTree.getHeight());

        if (subclassAbilityTree != null) {
            subclassAbilityTree.init(maxX);
            maxX += subclassAbilityTree.getWidth();
            maxY = Math.max(maxY, subclassAbilityTree.getHeight());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderInside(guiGraphics, mouseX, mouseY);
        renderWindow(guiGraphics);
        renderAbove(guiGraphics, mouseX, mouseY);

        if (mouseX >= left + WINDOW_INSIDE_X && mouseX < left + WINDOW_INSIDE_X + WINDOW_INSIDE_WIDTH
                && mouseY >= top + WINDOW_INSIDE_Y && mouseY < top + WINDOW_INSIDE_Y + WINDOW_INSIDE_HEIGHT) {
            renderTooltips(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int insideLeft = left + WINDOW_INSIDE_X;
        int insideTop = top + WINDOW_INSIDE_Y;

        guiGraphics.enableScissor(insideLeft, insideTop, insideLeft + WINDOW_INSIDE_WIDTH, insideTop + WINDOW_INSIDE_HEIGHT);
        guiGraphics.blit(TEXTURE, left, top, 0, WINDOW_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, 512, 512);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(insideLeft, insideTop, 0);

        mainAbilityTree.renderInside(guiGraphics, mouseX - insideLeft, mouseY - insideTop, panX, panY);
        subclassAbilityTree.renderInside(guiGraphics, mouseX - insideLeft, mouseY - insideTop, panX, panY);

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    private void renderAbove(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int insideLeft = left + WINDOW_INSIDE_X;
        int insideTop = top + WINDOW_INSIDE_Y;

        guiGraphics.enableScissor(insideLeft, insideTop, insideLeft + WINDOW_INSIDE_WIDTH, insideTop + WINDOW_INSIDE_HEIGHT);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(insideLeft, insideTop, 0);

        mainAbilityTree.renderAbove(guiGraphics, mouseX - insideLeft, mouseY - insideTop, panX, panY);
        subclassAbilityTree.renderAbove(guiGraphics, mouseX - insideLeft, mouseY - insideTop, panX, panY);

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    private void renderWindow(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 512, 512);
        guiGraphics.drawString(this.font, getTitle(), left + WINDOW_TITLE_X, top + WINDOW_TITLE_Y, 0x404040, false);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        double adjustedMouseX = mouseX - left - WINDOW_INSIDE_X - panX;
        double adjustedMouseY = mouseY - top - WINDOW_INSIDE_Y - panY;

        List<Component> tooltip = null;
        if (mainAbilityTree.isHovered(adjustedMouseX, adjustedMouseY)) {
            tooltip = mainAbilityTree.getTooltip(adjustedMouseX, adjustedMouseY);
        } else if (subclassAbilityTree.isHovered(adjustedMouseX, adjustedMouseY)) {
            tooltip = subclassAbilityTree.getTooltip(adjustedMouseX, adjustedMouseY);
        }

        if (tooltip != null) {
            guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - left - WINDOW_INSIDE_X - panX;
        double adjustedMouseY = mouseY - top - WINDOW_INSIDE_Y - panY;

        if (mainAbilityTree.isHovered(adjustedMouseX, adjustedMouseY)) {
            return mainAbilityTree.onClick(button, adjustedMouseX, adjustedMouseY);
        }

        if (subclassAbilityTree.isHovered(adjustedMouseX, adjustedMouseY)) {
            return subclassAbilityTree.onClick(button, adjustedMouseX, adjustedMouseY);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            this.isScrolling = false;
            return false;
        } else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else {
                scroll(dragX, dragY);
            }

            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll(scrollX * SCROLL_SPEED, scrollY * SCROLL_SPEED);
        return true;
    }

    private void scroll(double dragX, double dragY) {
        if (this.maxX > WINDOW_INSIDE_WIDTH) {
            this.panX = Mth.clamp(this.panX + dragX, -(this.maxX - WINDOW_INSIDE_WIDTH), 0.0);
        }

        if (this.maxY > WINDOW_INSIDE_HEIGHT) {
            this.panY = Mth.clamp(this.panY + dragY, -(this.maxY - WINDOW_INSIDE_HEIGHT), 0.0);
        }
    }
}
