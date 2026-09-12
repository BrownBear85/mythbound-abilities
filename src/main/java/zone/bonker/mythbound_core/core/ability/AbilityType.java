package zone.bonker.mythbound_core.core.ability;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import zone.bonker.mythbound_core.MythboundCore;

public enum AbilityType implements StringRepresentable {
    SPELL("spell", true, MythboundCore.identifier("textures/gui/spell_border.png")),
    PASSIVE("passive", false, MythboundCore.identifier("textures/gui/passive_border.png"));

    public static final Codec<AbilityType> CODEC = StringRepresentable.fromEnum(AbilityType::values);

    private final String name;
    private final boolean castable;
    private final ResourceLocation widgetTexture;

    AbilityType(String name, boolean castable, ResourceLocation widgetTexture) {
        this.name = name;
        this.widgetTexture = widgetTexture;
        this.castable = castable;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean isCastable() {
        return castable;
    }

    public ResourceLocation getWidgetTexture() {
        return widgetTexture;
    }
}
