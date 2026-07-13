package zone.bonker.mythbound_core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import zone.bonker.mythbound_core.core.ModelProperties;
import zone.bonker.mythbound_core.core.Race;

public class CharacterModelExtensions {
    private static final Multimap<ResourceLocation, LayerConstructor> LAYER_MAP = HashMultimap.create();

    public static void addExtraLayers(Race race, PlayerRenderer renderer, EntityModelSet modelSet) {
        for (LayerConstructor constructor : LAYER_MAP.get(race.getId())) {
            renderer.addLayer(constructor.construct(renderer, race.modelProperties(), modelSet));
        }
    }

    public static void addExtraLayers(ModelProperties.ModelPropContainer obj, PlayerRenderer renderer, EntityModelSet modelSet) {
        for (LayerConstructor constructor : LAYER_MAP.get(obj.getId())) {
            renderer.addLayer(constructor.construct(renderer, obj.modelProperties(), modelSet));
        }
    }

    public static void registerLayer(ResourceLocation id, LayerConstructor constructor) {
        LAYER_MAP.put(id, constructor);
    }

    public static boolean hasExtraLayers(ModelProperties.ModelPropContainer obj) {
        return LAYER_MAP.containsKey(obj.getId());
    }

    public interface LayerConstructor {
        RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> construct(PlayerRenderer renderer,
                                                                                       ModelProperties modelProperties,
                                                                                       EntityModelSet modelSet);
    }

    public static void reload() {
        LAYER_MAP.clear();

        registerLayer(ResourceLocation.fromNamespaceAndPath("mythbound", "frog"), (renderer, modelProperties, modelSet) -> new RenderLayer<>(renderer) {
            private final FrogModel<?> frogModel = new FrogModel<>(modelSet.bakeLayer(ModelLayers.FROG));

            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                poseStack.pushPose();
                poseStack.translate(0.4F, livingEntity.isCrouching() ? -1.3F : -1.5F, 0.0F);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(frogModel.renderType(BuiltInRegistries.FROG_VARIANT.getAny().orElseThrow().value().texture()));
                frogModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        });
    }
}
