package zone.bonker.mythbound_core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.*;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;

import javax.annotation.Nullable;
import java.util.*;

public class CharacterBuild implements OwnedAttachment {
    public static final ResourceLocation NONE = ResourceLocation.withDefaultNamespace("none");

    public static final Codec<CharacterBuild> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("race").forGetter(o -> o.raceId),
            ResourceLocation.CODEC.fieldOf("class").forGetter(o -> o.classId),
            ResourceLocation.CODEC.listOf().fieldOf("abilities").forGetter(o -> o.abilities),
            Codec.unboundedMap(ResourceLocation.CODEC, AbilityBinding.CODEC).fieldOf("bindings").forGetter(o -> o.bindings)
    ).apply(inst, CharacterBuild::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterBuild> NETWORK_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, o -> o.raceId,
            ResourceLocation.STREAM_CODEC, o -> o.classId,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.abilities,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, AbilityBinding.NETWORK_CODEC), o -> o.bindings,
            CharacterBuild::new);

    private ResourceLocation raceId = NONE;
    private ResourceLocation classId = NONE;
    private final List<ResourceLocation> abilities = new ArrayList<>();
    private final Map<ResourceLocation, AbilityBinding> bindings = new HashMap<>();
    private LivingEntity entity;

    public CharacterBuild(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("CharacterBuilder can only be attached to LivingEntities");
        }
        setOwner(livingEntity);
    }

    public CharacterBuild(ResourceLocation raceId, ResourceLocation classId, List<ResourceLocation> abilities, Map<ResourceLocation, AbilityBinding> bindings) {
        this.raceId = raceId;
        this.classId = classId;
        this.abilities.addAll(abilities);
        this.bindings.putAll(bindings);
    }

    //// STATIC HELPER METHODS

    public static boolean hasData(Entity entity) {
        return entity.hasData(MythboundAttachmentTypes.CHARACTER_BUILD);
    }

    public static CharacterBuild get(LivingEntity entity) {
        return entity.getData(MythboundAttachmentTypes.CHARACTER_BUILD);
    }

    public static Optional<CharacterBuild> getExisting(Entity entity) {
        return entity instanceof LivingEntity livingEntity && hasData(entity) ? Optional.of(get(livingEntity)) : Optional.empty();
    }

    public static boolean notCompatible(Race race, CharacterClass characterClass) {
        return !race.possibleClasses().contains(characterClass) && !characterClass.possibleRaceIds().contains(race.getId());
    }

    public static void refreshDimensions(LivingEntity entity) {
        entity.refreshDimensions();
        // Tell the client to refresh dimensions
        Pose pose = entity.getPose();
        entity.setPose(pose == Pose.STANDING ? Pose.CROUCHING : Pose.STANDING);
        entity.setPose(pose);
    }

    //// GETTERS

    @Nullable
    public Race getRace() {
        return raceId.equals(NONE) ? null : MythboundCore.RACES.getData().get(raceId);
    }

    @Nullable
    public CharacterClass getCharacterClass() {
        return classId.equals(NONE) ? null : MythboundCore.CLASSES.getData().get(classId);
    }

    public List<ResourceLocation> getUnlockedAbilityIds() {
        return abilities;
    }

    public Map<ResourceLocation, AbilityBinding> getAbilityBindings() {
        return bindings;
    }

    public boolean hasAbility(ResourceLocation id) {
        return entity.getData(MythboundAttachmentTypes.CHARACTER_BUILD).abilities.contains(id);
    }

    //// SETTERS

    public void save() {
        entity.setData(MythboundAttachmentTypes.CHARACTER_BUILD, this);
    }

    @Override
    public void setOwner(LivingEntity entity) {
        this.entity = entity;
    }

    public boolean setRace(@Nullable Race race) {
        ResourceLocation id = race == null ? NONE : race.getId();
        if (id.equals(raceId)) {
            return false;
        }

        Race oldRace = getRace();
        if (oldRace != null) {
            oldRace.deinitialize(entity);
        }

        CharacterClass characterClass = getCharacterClass();
        if (race == null || (characterClass != null && notCompatible(race, characterClass))) {
            setClass(null);
        }

        this.raceId = id;
        save();

        if (race != null) {
            race.initialize(entity);
        }
        return true;
    }

    public boolean setClass(@Nullable CharacterClass characterClass) {
        ResourceLocation id = characterClass == null ? NONE : characterClass.getId();
        if (id.equals(classId)) {
            return false;
        }

        Race race = getRace();
        if (race != null && characterClass != null && notCompatible(race, characterClass)) {
            return false;
        }

        CharacterClass oldClass = getCharacterClass();
        if (oldClass != null) {
            oldClass.deinitialize(entity);
        }

        this.classId = id;
        save();

        if (characterClass != null) {
            characterClass.initialize(entity);
        }
        return true;
    }

    public boolean unlockAbility(Ability ability) {
        if (abilities.contains(ability.getId())) {
            return false;
        }

        abilities.add(ability.getId());
        save();

        ability.initialize(entity);
        return true;
    }

    public boolean removeAbility(Ability ability) {
        if (!abilities.remove(ability.getId())) {
            return false;
        }

        ability.deinitialize(entity);
        save();
        return true;
    }

    public void setAbilityBinding(ResourceLocation id, AbilityBinding binding) {
        if (!abilities.contains(id) || binding.equals(bindings.get(id))) {
            return;
        }

        bindings.remove(id);
        if (!binding.isUnknown()) {
            bindings.put(id, binding);
        }
        save();
    }
}
