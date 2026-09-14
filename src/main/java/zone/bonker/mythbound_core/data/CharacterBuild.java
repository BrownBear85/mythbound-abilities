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
import zone.bonker.mythbound_core.client.MythboundCoreClient;
import zone.bonker.mythbound_core.core.*;
import zone.bonker.mythbound_core.core.ability.AbilityBinding;
import zone.bonker.mythbound_core.core.ability.AbilityTree;
import zone.bonker.mythbound_core.init.MythboundAttachmentTypes;

import javax.annotation.Nullable;
import java.util.*;

public class CharacterBuild extends OwnedAttachment {
    public static final ResourceLocation NONE = ResourceLocation.withDefaultNamespace("none");

    public static final Codec<CharacterBuild> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("race").forGetter(o -> o.raceId),
            ResourceLocation.CODEC.fieldOf("class").forGetter(o -> o.classId),
            ResourceLocation.CODEC.fieldOf("subclass").forGetter(o -> o.subclassId),
            Codec.INT.fieldOf("class_unlock_points").forGetter(o -> o.classUnlockPoints),
            Codec.INT.fieldOf("subclass_unlock_points").forGetter(o -> o.subclassUnlockPoints),
            ResourceLocation.CODEC.listOf().fieldOf("unlocked_class_abilities").forGetter(o -> o.unlockedClassAbilities),
            ResourceLocation.CODEC.listOf().fieldOf("unlocked_subclass_abilities").forGetter(o -> o.unlockedSubclassAbilities),
            Codec.unboundedMap(ResourceLocation.CODEC, AbilityBinding.CODEC).fieldOf("bindings").forGetter(o -> o.bindings)
    ).apply(inst, CharacterBuild::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharacterBuild> NETWORK_CODEC = MythboundSerialization.composite(
            ResourceLocation.STREAM_CODEC, o -> o.raceId,
            ResourceLocation.STREAM_CODEC, o -> o.classId,
            ResourceLocation.STREAM_CODEC, o -> o.subclassId,
            ByteBufCodecs.VAR_INT, o -> o.classUnlockPoints,
            ByteBufCodecs.VAR_INT, o -> o.subclassUnlockPoints,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.unlockedClassAbilities,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.unlockedSubclassAbilities,
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, AbilityBinding.NETWORK_CODEC), o -> o.bindings,
            CharacterBuild::new);

    private ResourceLocation raceId = NONE;
    private ResourceLocation classId = NONE;
    private ResourceLocation subclassId = NONE;
    private int classUnlockPoints;
    private int subclassUnlockPoints;
    private final ArrayList<ResourceLocation> unlockedClassAbilities = new ArrayList<>();
    private final ArrayList<ResourceLocation> unlockedSubclassAbilities = new ArrayList<>();
    private final Map<ResourceLocation, AbilityBinding> bindings = new HashMap<>();

    public CharacterBuild(IAttachmentHolder holder) {
        super(holder);
    }

    private CharacterBuild(ResourceLocation raceId, ResourceLocation classId, ResourceLocation subclassId,
                          int classUnlockPoints, int subclassUnlockPoints, List<ResourceLocation> unlockedClassAbilities,
                           List<ResourceLocation> unlockedSubclassAbilities, Map<ResourceLocation, AbilityBinding> bindings) {
        this.raceId = verify(raceId, MythboundCore.RACES);
        this.classId = verify(classId, MythboundCore.CLASSES);
        this.subclassId = verifySubclass(classId, subclassId);
        this.classUnlockPoints = classUnlockPoints;
        this.subclassUnlockPoints = subclassUnlockPoints;

        for (ResourceLocation abilityId : unlockedClassAbilities) {
            verify(abilityId, MythboundCore.ABILITIES);
        }
        for (ResourceLocation abilityId : unlockedSubclassAbilities) {
            verify(abilityId, MythboundCore.ABILITIES);
        }
        for (ResourceLocation abilityId : bindings.keySet()) {
            verify(abilityId, MythboundCore.ABILITIES);
        }

        this.unlockedClassAbilities.addAll(unlockedClassAbilities);
        this.unlockedSubclassAbilities.addAll(unlockedSubclassAbilities);
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
        return raceId.equals(NONE) ? null : MythboundCore.RACES.getOrThrow(raceId);
    }

    @Nullable
    public CharacterClass getCharacterClass() {
        return classId.equals(NONE) ? null : MythboundCore.CLASSES.getOrThrow(classId);
    }

    public Subclass getSubclass() {
        if (subclassId.equals(NONE)) {
            return null;
        }

        CharacterClass characterClass = getCharacterClass();
        return characterClass == null ? null : characterClass.subclasses().get(subclassId);
    }

    public List<ResourceLocation> getUnlockedAbilities() {
        return MythboundCore.ABILITIES.getData().keySet().stream().filter(this::hasAbility).toList();
    }

    public Map<ResourceLocation, AbilityBinding> getAbilityBindings() {
        return bindings;
    }

    public boolean hasAbility(ResourceLocation id) {
        if (unlockedClassAbilities.contains(id)) {
            return true;
        } else if (unlockedSubclassAbilities.contains(id)) {
            return true;
        }
        Race race = getRace();
        return race != null && race.inherentAbilities().stream().anyMatch(ability -> ability.getId().equals(id));
    }

    public int getClassUnlockPoints() {
        return classUnlockPoints;
    }

    public int getSubclassUnlockPoints() {
        return subclassUnlockPoints;
    }

    //// SETTERS

    public void save() {
        owner().setData(MythboundAttachmentTypes.CHARACTER_BUILD, this);
    }

    public void setRace(@Nullable Race race) {
        Race oldRace = getRace();
        if (oldRace != null) {
            oldRace.deinitialize(owner());
        }

        CharacterClass characterClass = getCharacterClass();
        if (race == null || (characterClass != null && notCompatible(race, characterClass))) {
            setClass(null);
        }

        this.raceId = race == null ? NONE : race.getId();
        save();

        if (race != null) {
            race.initialize(owner());
        }

        refreshDimensions(owner());
    }

    public void setClass(@Nullable CharacterClass characterClass) {
        CharacterClass oldClass = getCharacterClass();
        if (oldClass != null) {
            oldClass.deinitialize(owner());
        }

        this.classId = characterClass == null ? NONE : characterClass.getId();
        unlockedClassAbilities.clear();
        setSubclass(null);
        save();

        if (characterClass != null) {
            characterClass.initialize(owner());
            unlockAllFreeAbilities(characterClass.mainAbilityTree(), unlockedClassAbilities);
        }

        refreshDimensions(owner());
    }

    public void setSubclass(@Nullable ResourceLocation subclassId) {
        unlockedSubclassAbilities.clear();

        if (subclassId == null) {
            this.subclassId = NONE;
        } else {
            Subclass subclass = Objects.requireNonNull(getCharacterClass()).subclasses().get(subclassId);
            unlockAllFreeAbilities(subclass.abilityTree(), unlockedSubclassAbilities);
            this.subclassId = subclassId;
        }

        save();
    }

    public void unlockAbilityFromTree(AbilityTree abilityTree, AbilityTree.Node unlockedNode, boolean mainAbilityTree) {
        MythboundCore.ABILITIES.getOrThrow(unlockedNode.abilityId()).initialize(owner());

        if (mainAbilityTree) {
            classUnlockPoints -= unlockedNode.cost();
            unlockedClassAbilities.add(unlockedNode.abilityId());
            unlockAllFreeAbilities(abilityTree, unlockedClassAbilities);
        } else {
            subclassUnlockPoints -= unlockedNode.cost();
            unlockedSubclassAbilities.add(unlockedNode.abilityId());
            unlockAllFreeAbilities(abilityTree, unlockedSubclassAbilities);
        }

        save();
    }

    private void unlockAllFreeAbilities(AbilityTree abilityTree, ArrayList<ResourceLocation> unlockedAbilities) {
        for (AbilityTree.Node node : abilityTree.nodes()) {
            if (node.cost() > 0
                    || unlockedAbilities.contains(node.abilityId())
                    || !unlockedAbilities.containsAll(node.requiredAbilities())) {
                continue;
            }

            unlockedAbilities.add(node.abilityId());
            save();
        }
    }

    public void setAbilityBinding(ResourceLocation id, AbilityBinding binding) {
        bindings.remove(id);
        if (!binding.isNoBind()) {
            bindings.put(id, binding);
        }
        save();
    }

    public void setPoints(int classPoints, int subclassPoints) {
        this.classUnlockPoints = classPoints;
        this.subclassUnlockPoints = subclassPoints;
        save();
    }

    //// MISC METHODS

    private static <T> ResourceLocation verify(ResourceLocation id, ReloadableJsonRegistry<T> registry) {
        if (id.equals(NONE)) {
            return id;
        }
        registry.getOrThrow(id);
        return id;
    }

    private static ResourceLocation verifySubclass(ResourceLocation classId, ResourceLocation subclassId) {
        if (subclassId.equals(NONE)) {
            return subclassId;
        }

        if (classId.equals(NONE)) {
            throw new IllegalArgumentException("Class " + classId + " has no subclass with id " + subclassId);
        }
        CharacterClass characterClass = MythboundCore.CLASSES.getOrThrow(classId);
        if (!characterClass.subclasses().containsKey(subclassId)) {
            throw new IllegalArgumentException("Class " + classId + " has no subclass with id " + subclassId);
        }
        return subclassId;
    }

    @Override
    public void onUpdatedOnClient() {
        MythboundCoreClient.refreshCurrentScreen(this);
    }
}
