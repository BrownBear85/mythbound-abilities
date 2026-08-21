package zone.bonker.mythbound_core.client.gui;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.networking.C2SUnlockAbilityPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AbilityWidget implements HoverableClickable {
    private static final ResourceLocation TEXTURE = MythboundCore.identifier("textures/gui/ability_widget.png");

    private static final int CONNECTOR_LINE_U = 128;
    private static final int CONNECTOR_LINE_V = 0;
    private static final int CONNECTOR_LINE_WIDTH = 4;
    private static final int CONNECTOR_LINE_HEIGHT = 2;

    private final AbilityTree abilityTree;
    private final AbilityTree.Node node;
    private final boolean mainAbilityTree;
    private final Ability ability;
    private final List<Component> tooltip = new ArrayList<>();
    private final int x, y;
    private Status status;

    public AbilityWidget(AbilityTree abilityTree, AbilityTree.Node node, boolean mainAbilityTree) {
        this.abilityTree = abilityTree;
        this.node = node;
        this.mainAbilityTree = mainAbilityTree;
        this.ability = MythboundCore.ABILITIES.getData().get(node.abilityId());
        this.x = node.column() * AbilityTreesScreen.WIDGET_WIDTH;
        this.y = node.row() * AbilityTreesScreen.WIDGET_HEIGHT;
    }

    public void refreshStatus(CharacterBuild characterBuild, Supplier<Integer> pointGetter) {
        if (characterBuild.hasAbility(node.abilityId())) {
            status = Status.UNLOCKED;
        } else if (node.requiredAbilities().stream().anyMatch(ownedId -> !characterBuild.hasAbility(ownedId))) {
            status = Status.LOCKED;
        } else {
            status = pointGetter.get() >= node.cost() ? Status.CAN_AFFORD : Status.CANNOT_AFFORD;
        }

        tooltip.clear();
        tooltip.add(ability.name());
        tooltip.addAll(ability.description());
        tooltip.add(Component.empty());

        switch(status) {
            case LOCKED -> {
                List<Ability> requiredAbilities = node.requiredAbilities().stream()
                        .filter(requiredId -> !characterBuild.hasAbility(requiredId))
                        .map(MythboundCore.ABILITIES.getData()::get)
                        .filter(Predicates.notNull())
                        .toList();

                if (requiredAbilities.isEmpty()) {
                    return;
                }

                MutableComponent listComponent = requiredAbilities.getFirst().name().copy();
                for (int i = 1; i < requiredAbilities.size(); i++) {
                    Ability requiredAbility = requiredAbilities.get(i);
                    listComponent = listComponent.append(", ").append(requiredAbility.name());
                }

                tooltip.add(Component.translatable("gui.mythbound_core.ability_tree.node_requirements", listComponent).withStyle(ChatFormatting.RED));
            }
            case CANNOT_AFFORD -> tooltip.add(Component.translatable("gui.mythbound_core.ability_tree." + (node.cost() == 1 ? "node_cannot_afford" : "node_cannot_afford_plural"), node.cost()).withStyle(ChatFormatting.RED));
            case CAN_AFFORD -> tooltip.add(Component.translatable("gui.mythbound_core.ability_tree." + (node.cost() == 1 ? "node_click_to_unlock" : "node_click_to_unlock_plural"), node.cost()).withStyle(ChatFormatting.GREEN));
            case UNLOCKED -> tooltip.add(Component.translatable("gui.mythbound_core.ability_tree.node_unlocked").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + AbilityTreesScreen.WIDGET_WIDTH && mouseY >= y && mouseY < y + AbilityTreesScreen.WIDGET_HEIGHT;
    }

    public void renderLines(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        float thisCenterX = x + 0.5F * AbilityTreesScreen.WIDGET_WIDTH;
        float thisCenterY = y + 0.5F * AbilityTreesScreen.WIDGET_HEIGHT;

        for (ResourceLocation requiredId : node.requiredAbilities()) {
            AbilityTree.Node requiredNode = abilityTree.getNodeForAbility(requiredId);
            if (requiredNode == null) {
                continue;
            }

            float otherCenterX = (requiredNode.column() + 0.5F) * AbilityTreesScreen.WIDGET_WIDTH;
            float otherCenterY = (requiredNode.row() + 0.5F) * AbilityTreesScreen.WIDGET_HEIGHT;

            float deltaX = otherCenterX - thisCenterX;
            float deltaY = otherCenterY - thisCenterY;
            float length = Mth.sqrt(deltaX * deltaX + deltaY * deltaY);
            float angle = (float) Mth.atan2(deltaY, deltaX);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(thisCenterX, thisCenterY, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotation(angle));

            if (status == Status.UNLOCKED || status == Status.CAN_AFFORD) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
            } else {
                RenderSystem.setShaderColor(0.7F, 0.7F, 0.7F, 0.5F);
            }

            for (int offset = 0; offset < length; offset += CONNECTOR_LINE_WIDTH) {
                guiGraphics.blit(TEXTURE, offset, -CONNECTOR_LINE_HEIGHT / 2, CONNECTOR_LINE_U, CONNECTOR_LINE_V, CONNECTOR_LINE_WIDTH, CONNECTOR_LINE_HEIGHT);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().popPose();
        }
    }

    public void renderSelf(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(TEXTURE,
                x,
                y,
                status.ordinal() * AbilityTreesScreen.WIDGET_WIDTH,
                isHovered(mouseX, mouseY) ? AbilityTreesScreen.WIDGET_HEIGHT : 0,
                AbilityTreesScreen.WIDGET_WIDTH,
                AbilityTreesScreen.WIDGET_HEIGHT);
    }

    @Override
    public List<Component> getTooltip(double mouseX, double mouseY) {
        return tooltip;
    }

    @Override
    public boolean onClick(int button, double mouseX, double mouseY) {
        if (status == Status.CAN_AFFORD) {
            PacketDistributor.sendToServer(new C2SUnlockAbilityPacket(node.abilityId(), mainAbilityTree));
            return true;
        }
        return false;
    }

    public enum Status {
        LOCKED, CANNOT_AFFORD, CAN_AFFORD, UNLOCKED
    }
}
