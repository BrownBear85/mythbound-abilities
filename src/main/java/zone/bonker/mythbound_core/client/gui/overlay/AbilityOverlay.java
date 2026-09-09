package zone.bonker.mythbound_core.client.gui.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.AbilityInputHandler;
import zone.bonker.mythbound_core.client.gui.screen.ability_tree.AbilityWidget;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityBinding;
import zone.bonker.mythbound_core.data.CharacterBuild;

import java.util.*;
import java.util.List;

public class AbilityOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation ID = MythboundCore.identifier("ability_overlay");

    private static final int ABILITY_SIZE = AbilityWidget.ICON_SIZE;
    private static final int MIN_SPACING = 8;
    private static final int MARGIN = 32;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        Optional<CharacterBuild> optional = CharacterBuild.getExisting(Minecraft.getInstance().player);
        if (optional.isEmpty()) {
            return;
        }

        CharacterBuild characterBuild = optional.get();
        if (characterBuild.getAbilityBindings().isEmpty()) {
            return;
        }

        int x = MIN_SPACING;
        int y = MARGIN;
        int maxWidth = ABILITY_SIZE + MIN_SPACING;

        List<ResourceLocation> ids = new ArrayList<>(characterBuild.getAbilityBindings().keySet());
        Collections.sort(ids);

        for (ResourceLocation id : ids) {
            Ability ability = MythboundCore.ABILITIES.getData().get(id);
            if (ability == null) {
                continue;
            }

            Vector2ic size = renderAbility(guiGraphics, ability, characterBuild.getAbilityBindings().get(id), x, y);

            if (size.x() > maxWidth) {
                maxWidth = size.x();
            }
            y += size.y();
            if (y >= guiGraphics.guiHeight() - MARGIN) {
                y = MARGIN;
                x += maxWidth;
            }
        }
    }

    private Vector2ic renderAbility(GuiGraphics guiGraphics, Ability ability, AbilityBinding binding, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + ABILITY_SIZE + 1, y + ABILITY_SIZE + 1, 0x55888888);

        guiGraphics.blit(ability.getTexture(),
                x,
                y,
                0,
                0,
                ABILITY_SIZE,
                ABILITY_SIZE,
                ABILITY_SIZE,
                ABILITY_SIZE);

        Font font = Minecraft.getInstance().font;
        Component text = Component.literal(AbilityInputHandler.getDisplayName(binding).getString().replace(" + ", "+"));
        List<FormattedCharSequence> lines = font.split(text, ABILITY_SIZE * 3);

        int maxWidth = 0;
        for (FormattedCharSequence line : lines) {
            int width = font.width(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        int textX = Math.max(x - MIN_SPACING / 2, x + ABILITY_SIZE - maxWidth / 2 + 1);
        int textY = y + ABILITY_SIZE - 2;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            guiGraphics.drawString(font, line, textX, textY + i * 9, 0xFFEEEEEE, true);
        }

        return new Vector2i(textX - x + maxWidth + MIN_SPACING, Math.max(ABILITY_SIZE + MIN_SPACING, textY - y + lines.size() * 9 + 4));
    }
}
