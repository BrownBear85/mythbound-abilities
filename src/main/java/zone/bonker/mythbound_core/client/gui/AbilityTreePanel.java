package zone.bonker.mythbound_core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import zone.bonker.mythbound_core.core.NamedAndDescribed;
import zone.bonker.mythbound_core.data.CharacterBuild;

import javax.annotation.Nullable;

public abstract class AbilityTreePanel implements HoverableClickable {
    protected final boolean rightMostPanel;
    protected int width;
    protected int height;
    protected int offset;

    public AbilityTreePanel(boolean rightMostPanel) {
        this.rightMostPanel = rightMostPanel;
    }

    public abstract boolean isInvalid(@Nullable NamedAndDescribed namedAndDescribed);

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= offset && mouseX < offset + width && mouseY >= 0 && mouseY < height;
    }

    public final void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, double panX, double panY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(offset + panX, panY, 0);
        drawInside(guiGraphics, mouseX - offset - panX, mouseY - panY);
        guiGraphics.pose().popPose();
    }

    public final void renderAbove(GuiGraphics guiGraphics, int mouseX, int mouseY, double panX, double panY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(offset + panX, 0, 0);
        drawAbove(guiGraphics, mouseX - offset - panX, mouseY);
        guiGraphics.pose().popPose();
    }

    protected void drawInside(GuiGraphics guiGraphics, double mouseX, double mouseY) {

    }

    protected void drawAbove(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (!rightMostPanel) {
            RenderSystem.enableBlend();
            guiGraphics.blit(AbilityTreesScreen.TEXTURE, width - 1, -1, 340, 0, 5, AbilityTreesScreen.WINDOW_INSIDE_HEIGHT + 1, 512, 512);
        }
    }

    public void init(int offset) {
        this.offset = offset;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void refreshWidgets(CharacterBuild characterBuild) {

    }

    @Nullable
    public AbilityWidget getWidget(double mouseX, double mouseY) {
        return null;
    }
}
