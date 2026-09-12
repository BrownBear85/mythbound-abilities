package zone.bonker.mythbound_core.client.gui.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.joml.Vector2ic;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.MythboundRendering;
import zone.bonker.mythbound_core.core.ability.MagicUnitDefinition;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.data.SpellData;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;

import java.util.HashSet;
import java.util.Set;

public class UnitsOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation ID = MythboundCore.identifier("units_overlay");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().player == null
                || !Minecraft.getInstance().player.hasData(MythboundAttachmentTypes.SPELL_DATA)
                || !CharacterBuild.hasData(Minecraft.getInstance().player)) {
            return;
        }

        CharacterBuild characterBuild = CharacterBuild.get(Minecraft.getInstance().player);
        Set<ResourceLocation> relevantUnitIds = new HashSet<>();
        for (ResourceLocation abilityId : characterBuild.getAbilityBindings().keySet()) {
            relevantUnitIds.addAll(MythboundCore.ABILITIES.getOrThrow(abilityId).cost().getRelevantUnits());
        }

        if (relevantUnitIds.isEmpty()) {
            return;
        }

        int globalBarX = 5;
        int globalBarY = guiGraphics.guiHeight() - 25;

        SpellData spellData = SpellData.get(Minecraft.getInstance().player);
        for (ResourceLocation unitId : relevantUnitIds) {
            MagicUnitDefinition unitDefinition = MythboundCore.MAGIC_UNITS.getOrThrow(unitId);

            switch (unitDefinition.display()) {
                case ABILITY_SPECIFIC:
                    ResourceLocation abilityAssociation = spellData.getAssociation(unitId);
                    if (abilityAssociation != null) {
                        Vector2ic abilityPos = AbilityOverlay.ABILITY_LOCATIONS.get(abilityAssociation);
                        if (abilityPos != null) {
                            renderAbilitySpecificBar(guiGraphics, abilityPos.x(), abilityPos.y(), 3, 16, spellData.getUnitAmount(unitId), unitDefinition);
                            break;
                        }
                    }
                case GLOBAL:
                    renderGlobalUnitBar(guiGraphics, globalBarX, globalBarY, 90, 10, spellData.getUnitAmount(unitId), unitDefinition);
                    globalBarY -= 12;
            }
        }
    }

    private void renderAbilitySpecificBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int amount, MagicUnitDefinition definition) {
        float minX = x + 20;
        float minY = y + (AbilityOverlay.ABILITY_SIZE - height) / 2F;
        MythboundRendering.fillF(guiGraphics, minX, minY, minX + width, minY + height, getBgColor(definition));
        MythboundRendering.fillF(guiGraphics, minX, minY + height - height * amount / (float) definition.maximum(), minX + width, minY + height, getBarColor(definition));
    }

    private void renderGlobalUnitBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int amount, MagicUnitDefinition definition) {
        MythboundRendering.fillF(guiGraphics, (float) x, y, (float) x + width, y + 10, getBgColor(definition));
        MythboundRendering.fillF(guiGraphics, (float) x, y, (float) x + width * amount / (float) definition.maximum(), y + 9, getBarColor(definition));

        Font font = Minecraft.getInstance().font;
        String text = definition.name().getString() + ": " + amount;
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text, x + (width - textWidth) / 2F + 1, y + 2, getBgColor(definition), false);
        guiGraphics.drawString(font, text, x + (width - textWidth) / 2F, y + 1, 0xFFFAFAFA, false);
    }

    private int getBarColor(MagicUnitDefinition definition) {
        return definition.color() | 0xFF000000;
    }

    private int getBgColor(MagicUnitDefinition definition) {
        int r = FastColor.ARGB32.red(definition.color());
        int g = FastColor.ARGB32.green(definition.color());
        int b = FastColor.ARGB32.blue(definition.color());
        return FastColor.ARGB32.color(255, r * 2 / 5, g * 2 / 5, b * 2 / 5);
    }
}
