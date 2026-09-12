package zone.bonker.mythbound_core.client.gui.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.ClientSpellTargeting;
import zone.bonker.mythbound_core.core.ability.AbilityTargeting;

public class TargetingOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation ID = MythboundCore.identifier("targeting_overlay");

    private static final ResourceLocation ENEMY_TARGET_ARROW = MythboundCore.identifier("enemy_target_arrow");
    private static final ResourceLocation ALLY_TARGET_ARROW = MythboundCore.identifier("ally_target_arrow");
    private static final ResourceLocation SELECTED_TARGET_ARROW = MythboundCore.identifier("selected_target_arrow");
    private static final int SPRITE_SIZE = 15;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        LivingEntity primaryTarget = ClientSpellTargeting.getSelectedTarget();
        if (primaryTarget == null) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        for (LivingEntity entity : ClientSpellTargeting.getAllTargets()) {
            drawArrow(guiGraphics, entity, entity == primaryTarget, partialTick);
        }
    }

    private void drawArrow(GuiGraphics guiGraphics, LivingEntity entity, boolean selected, float partialTick) {
        Vector3f screenPos = ClientSpellTargeting.projectWorldPos(getEntityTop(entity, partialTick));
        if (screenPos != null) {
            float x = (screenPos.x * 0.5F + 0.5F) * guiGraphics.guiWidth();
            float y = (screenPos.y * -0.5F + 0.5F) * guiGraphics.guiHeight();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x - 7.5F, y - 10F, 0);
            if (selected) {
                guiGraphics.blitSprite(SELECTED_TARGET_ARROW, 0, 0, SPRITE_SIZE, SPRITE_SIZE);
            }
            guiGraphics.blitSprite(AbilityTargeting.isAlly(entity) ? ALLY_TARGET_ARROW : ENEMY_TARGET_ARROW, 0, 0, SPRITE_SIZE, SPRITE_SIZE);
            guiGraphics.pose().popPose();
        }
    }

    private Vec3 getEntityTop(LivingEntity entity, float partialTick) {
        return entity.getPosition(partialTick).add(0, entity.getBbHeight(), 0);
    }
}
