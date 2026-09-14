package zone.bonker.mythbound_core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import zone.bonker.mythbound_core.MythboundCore;
import zone.bonker.mythbound_core.core.ability.Ability;
import zone.bonker.mythbound_core.core.ability.AbilityTargeting;
import zone.bonker.mythbound_core.data.CharacterBuild;

import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber
public class ClientSpellTargeting {
    private static final double MAX_DIST_TO_CROSSHAIR = 0.5;

    private static final Matrix4f MVP = new Matrix4f();
    private static Vec3 cameraPos = Vec3.ZERO;

    @Nullable
    private static LivingEntity primaryTarget;
    private static final List<LivingEntity> sortedTargets = new ArrayList<>();

    private static boolean canTargetEnemies;
    private static boolean canTargetAllies;
    private static double maxRange;

    private static boolean manualTargeting;

    @Nullable
    public static LivingEntity getSelectedTarget() {
        return primaryTarget;
    }

    public static Iterable<LivingEntity> getAllTargets() {
        return sortedTargets;
    }

    @Nullable
    public static LivingEntity getTargetForAbility(LocalPlayer player, Ability ability) {
        if (ability.targeting().allowSelf()) {
            if ((!ability.targeting().allowAllies() && !ability.targeting().allowEnemies()) || primaryTarget == null) {
                return player;
            }
        }

        if (ability.targeting().matches(primaryTarget, null)) {
            return primaryTarget;
        }

        for (LivingEntity entity : getAllTargets()) {
            if (ability.targeting().matches(entity, null)) {
                return entity;
            }
        }

        return null;
    }

    public static void reset() {
        primaryTarget = null;
        sortedTargets.clear();
    }

    public static void tick() {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null) {
            reset();
            return;
        }

        CharacterBuild characterBuild = CharacterBuild.get(player);
        if (characterBuild.getAbilityBindings().isEmpty()) {
            reset();
            return;
        }

        if (player.tickCount % 4 != 0) {
            return;
        }

        canTargetEnemies = false;
        canTargetAllies = false;

        for (ResourceLocation abilityId : characterBuild.getAbilityBindings().keySet()) {
            Ability ability = MythboundCore.ABILITIES.getOrThrow(abilityId);
            canTargetEnemies = canTargetEnemies || ability.targeting().allowEnemies();
            canTargetAllies = canTargetAllies || ability.targeting().allowAllies();
            maxRange = Math.max(maxRange, ability.targeting().maxRange());
        }

        if (!canTargetEnemies && !canTargetAllies) {
            reset();
            return;
        }

        refreshTargets(level, player);
    }

    public static void pressedKeybind() {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        manualTargeting = true;

        if (sortedTargets.size() > 1) {
            int nextIndex = sortedTargets.indexOf(primaryTarget) + 1;
            if (nextIndex >= sortedTargets.size()) {
                nextIndex = 0;
            }
            primaryTarget = sortedTargets.get(nextIndex);
        }
    }

    private static void refreshTargets(ClientLevel level, LocalPlayer player) {
        Map<LivingEntity, Double> targetDistanceMap = new HashMap<>();

        level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(cameraPos, maxRange, maxRange, maxRange))
                .stream()
                .filter(e -> e != player
                        && e.isAlive()
                        && canTarget(e, canTargetAllies, canTargetEnemies)
                        && e.getEyePosition().distanceTo(cameraPos) <= maxRange
                        && hasLineOfSight(level, e)
                )
                .forEach(e -> targetDistanceMap.put(e, getDistanceToCrosshair(e.position().add(0, e.getBbHeight() * 0.5, 0))));

        targetDistanceMap.entrySet().removeIf(entry -> entry.getValue() > MAX_DIST_TO_CROSSHAIR);

        sortedTargets.clear();
        sortedTargets.addAll(targetDistanceMap.keySet());
        sortedTargets.sort(Comparator.comparingDouble(targetDistanceMap::get));

        int maxTargets = 50;
        while (sortedTargets.size() > maxTargets) {
            sortedTargets.removeLast();
        }

        if (!sortedTargets.isEmpty() && (!manualTargeting || primaryTarget == null || !sortedTargets.contains(primaryTarget))) {
            manualTargeting = false;
            primaryTarget = sortedTargets.getFirst();
        }
    }

    private static boolean canTarget(LivingEntity entity, boolean targetAllies, boolean targetEnemies) {
        if (AbilityTargeting.isAlly(entity)) {
            return targetAllies;
        }
        return targetEnemies;
    }

    private static boolean hasLineOfSight(ClientLevel level, LivingEntity entity) {
        Vec3 entityEyePos = entity.getEyePosition();
        HitResult hitResult = level.clip(new ClipContext(cameraPos, entityEyePos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hitResult.getType() == HitResult.Type.MISS;
    }

    public static double getDistanceToCrosshair(Vec3 point) {
        Vector3f screenPos = projectWorldPos(point);
        if (screenPos == null) {
            return 999999;
        }
        Vector3f crosshairPos = new Vector3f(0, 0, screenPos.z);
        return crosshairPos.distance(screenPos);
    }

    @Nullable
    public static Vector3f projectWorldPos(Vec3 point) {
        Vec3 relative = point.subtract(cameraPos);
        Vector3f screenPos = MVP.transformProject(relative.toVector3f());
        return screenPos.z > 1.0 ? null : screenPos;
    }

    @SubscribeEvent
    public static void afterRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            event.getProjectionMatrix().mul(event.getModelViewMatrix(), MVP);
            cameraPos = event.getCamera().getPosition();
        }
    }

    @SubscribeEvent
    public static void entityLeaveLevel(EntityLeaveLevelEvent event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player == event.getEntity()) {
            reset();
        }
    }

    @SubscribeEvent
    public static void postClientTick(ClientTickEvent.Post event) {
        tick();
    }
}
