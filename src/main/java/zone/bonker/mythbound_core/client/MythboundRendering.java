package zone.bonker.mythbound_core.client;

import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class MythboundRendering {
    public static void drawCenteredOutlinedString(GuiGraphics guiGraphics, Font font, String str, int x, int y, int textColor, int outlineColor) {
        x -= font.width(str) / 2;
        guiGraphics.drawString(font, str, x + 1, y, outlineColor, false);
        guiGraphics.drawString(font, str, x - 1, y, outlineColor, false);
        guiGraphics.drawString(font, str, x, y + 1, outlineColor, false);
        guiGraphics.drawString(font, str, x, y - 1, outlineColor, false);
        guiGraphics.drawString(font, str, x, y, textColor, false);
    }

    public static void drawLine(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, int width, int color) {
        float deltaX = x1 - x0;
        float deltaY = y1 - y0;
        float length = Mth.sqrt(deltaX * deltaX + deltaY * deltaY);
        float angle = (float) Mth.atan2(deltaY, deltaX);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x0, y0, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotation(angle));
        guiGraphics.fill(0, -width / 2, Mth.ceil(length), width / 2, color);
        guiGraphics.pose().popPose();
    }

    public static void fillF(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x0, y0, 0);
        guiGraphics.pose().scale(x1 - x0, y1 - y0, 1);
        guiGraphics.fill(0, 0, 1, 1, color);
        guiGraphics.pose().popPose();
    }
}
