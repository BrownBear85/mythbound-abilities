package zone.bonker.mythbound_core.client.gui.screen.ability_tree;

import com.google.common.base.Predicates;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.client.AbilityInputHandler;
import zone.bonker.mythbound_core.client.MythboundRendering;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityBinding;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.data.CharacterBuild;
import zone.bonker.mythbound_core.networking.C2SUnlockAbilityPacket;

import java.util.ArrayList;
import java.util.List;

public class AbilityWidget implements HoverableClickable {
    public static final int SIZE = 32;
    public static final int ICON_SIZE = 16;
    public static final int PADDING = (SIZE - ICON_SIZE) / 2;

    private static final ResourceLocation TEXTURE = MythboundCore.identifier("textures/gui/ability_widget.png");

    private final AbilityTree abilityTree;
    private final AbilityTree.Node node;
    private final boolean mainAbilityTree;
    private final Ability ability;
    private final List<Component> tooltip = new ArrayList<>();
    private final int x, y;
    private Status status;
    private CharacterBuild characterBuild;

    public AbilityWidget(AbilityTree abilityTree, AbilityTree.Node node, boolean mainAbilityTree) {
        this.abilityTree = abilityTree;
        this.node = node;
        this.mainAbilityTree = mainAbilityTree;
        this.ability = MythboundCore.ABILITIES.getData().get(node.abilityId());
        this.x = node.column() * SIZE;
        this.y = node.row() * SIZE;
    }

    public void refreshStatus(CharacterBuild characterBuild) {
        this.characterBuild = characterBuild;

        if (characterBuild.hasAbility(node.abilityId())) {
            status = Status.UNLOCKED;
        } else if (node.requiredAbilities().stream().anyMatch(ownedId -> !characterBuild.hasAbility(ownedId))) {
            status = Status.LOCKED;
        } else {
            int points = mainAbilityTree ? characterBuild.getClassUnlockPoints() : characterBuild.getSubclassUnlockPoints();
            status = points >= node.cost() ? Status.CAN_AFFORD : Status.CANNOT_AFFORD;
        }

        refreshTooltip();
    }

    private void refreshTooltip() {
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
            case UNLOCKED -> {
                tooltip.add(Component.translatable("gui.mythbound_core.ability_tree.node_unlocked").withStyle(ChatFormatting.DARK_GRAY));

                AbilityBinding binding = characterBuild.getAbilityBindings().get(ability.getId());
                boolean waitingForInput = ability.getId().equals(AbilityInputHandler.abilityToBind);
                MutableComponent component = binding == null
                        ? Component.translatable("gui.mythbound_core.ability_tree.node_no_bind")
                        : AbilityInputHandler.getDisplayName(binding).copy();

                if (waitingForInput) {
                    component = Component.literal("> ").withStyle(ChatFormatting.YELLOW).append(component.withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
                } else {
                    component = component.withStyle(ChatFormatting.GRAY);
                }

                component = Component.translatable("gui.mythbound_core.ability_tree.node_bound", component).withStyle(ChatFormatting.GRAY);

                tooltip.add(component);
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + SIZE && mouseY >= y && mouseY < y + SIZE;
    }

    public void renderLines(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        float thisCenterX = x + 0.5F * SIZE;
        float thisCenterY = y + 0.5F * SIZE;

        for (ResourceLocation requiredId : node.requiredAbilities()) {
            AbilityTree.Node requiredNode = abilityTree.getNodeForAbility(requiredId);
            if (requiredNode == null) {
                continue;
            }

            boolean heavyLine = status == AbilityWidget.Status.UNLOCKED || status == AbilityWidget.Status.CAN_AFFORD;
            MythboundRendering.drawLine(guiGraphics,
                    thisCenterX,
                    thisCenterY,
                    (requiredNode.column() + 0.5F) * SIZE,
                    (requiredNode.row() + 0.5F) * SIZE,
                    2,
                    heavyLine ? 0xDDFFFFFF : 0x88AAAAAA);
        }
    }

    public void renderSelf(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(TEXTURE,
                x,
                y,
                status.ordinal() * SIZE,
                isHovered(mouseX, mouseY) ? SIZE : 0,
                SIZE,
                SIZE);
        
        guiGraphics.blit(ability.getTexture(),
                x + PADDING,
                y + PADDING,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE);
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
        } else if (status == Status.UNLOCKED) {
            AbilityInputHandler.abilityToBind = ability.getId();
            refreshTooltip();
        }
        return false;
    }

    public enum Status {
        LOCKED, CANNOT_AFFORD, CAN_AFFORD, UNLOCKED
    }
}
