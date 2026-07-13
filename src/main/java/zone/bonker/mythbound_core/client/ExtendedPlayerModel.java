package zone.bonker.mythbound_core.client;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import zone.bonker.mythbound_core.core.ModelProperties;

public class ExtendedPlayerModel<T extends LivingEntity> extends PlayerModel<T> {
    private final ModelProperties modelProperties;

    public ExtendedPlayerModel(boolean slim, ModelProperties modelProperties) {
        super(createLayer(slim, modelProperties).bakeRoot(), slim);
        this.modelProperties = modelProperties;
    }

    public static LayerDefinition createLayer(boolean slim, ModelProperties modelProperties) {
        PlayerModelBuilder builder = PlayerModelBuilder.create();

        // Skin
        builder.box("left_leg").tex(16, 48).positioned(-2, 0, -2).sized(4, 12, 4).pivot(1.9F, 12.0F, 0.0F).scale(modelProperties.legs());
        builder.box("right_leg").tex(0, 16).positioned(-2, 0, -2).sized(4, 12, 4).pivot(-1.9F, 12.0F, 0.0F).scale(modelProperties.legs());
        builder.box("body").tex(16, 16).positioned(-4, 0, -2).sized(8, 12, 4).scale(modelProperties.body());
        builder.box("left_arm").tex(32, 48).positioned(-1, -2, -2).sized(slim ? 3 : 4, 12, 4).pivot(5.0F, slim ? 2.5F : 2.0F, 0.0F).scale(modelProperties.arms());
        builder.box("right_arm").tex(40, 16).positioned(slim ? -2 : -3, -2, -2).sized(slim ? 3 : 4, 12, 4).pivot(5.0F, slim ? 2.5F : 2.0F, 0.0F).scale(modelProperties.arms());
        builder.box("head").tex(0, 0).positioned(-4, -8, -4).sized(8, 8, 8).scale(modelProperties.head());

        // Skin overlay
        builder.get("left_leg").clone("left_pants").tex(0, 48).grow(0.25F);
        builder.get("right_leg").clone("right_pants").tex(0, 32).grow(0.25F);
        builder.get("body").clone("jacket").tex(16, 32).grow(0.25F);
        builder.get("left_arm").clone("left_sleeve").tex(48, 48).grow(0.25F);
        builder.get("right_arm").clone("right_sleeve").tex(40, 32).grow(0.25F);
        builder.get("head").clone("hat").tex(32, 0).grow(0.5F);

        // deadmau5 ears
        builder.box("ear").tex(24, 0).positioned(-3, -6, -1).sized(6, 6, 1);

        // Cape
        builder.box("cloak").tex(0, 0).positioned(-5, 0, -1).sized(10, 16, 1).texScale(1.0F, 0.5F);

        return LayerDefinition.create(builder.build(), 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (ModelPart part : parts) {
            part.resetPose();
        }

        // From HumanoidModel

        boolean fallFlying = entity.getFallFlyingTicks() > 4;
        boolean swimming = entity.isVisuallySwimming();

        // Head pose
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        if (fallFlying) {
            head.xRot = -Mth.PI / 4;
        } else if (swimAmount > 0.0F) {
            if (swimming) {
                head.xRot = rotlerpRad(swimAmount, head.xRot, -Mth.PI / 4);
            } else {
                head.xRot = rotlerpRad(swimAmount, head.xRot, headPitch * Mth.DEG_TO_RAD);
            }
        } else {
            head.xRot = headPitch * Mth.DEG_TO_RAD;
        }

        //// Limbs and body

        body.yRot = 0.0F;
        rightArm.z = 0.0F;
        rightArm.x = -5.0F;
        leftArm.z = 0.0F;
        leftArm.x = 5.0F;
        float limbStiffness = 1.0F;
        if (fallFlying) {
            limbStiffness = (float) entity.getDeltaMovement().lengthSqr();
            limbStiffness /= 0.2F;
            limbStiffness *= limbStiffness * limbStiffness;
        }

        if (limbStiffness < 1.0F) {
            limbStiffness = 1.0F;
        }

        rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * limbSwingAmount / limbStiffness;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount / limbStiffness;
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / limbStiffness;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount / limbStiffness;
        rightLeg.yRot = 0.005F;
        leftLeg.yRot = -0.005F;
        rightLeg.zRot = 0.005F;
        leftLeg.zRot = -0.005F;
        if (riding) {
            rightArm.xRot += -Mth.PI / 5;
            leftArm.xRot += -Mth.PI / 5;
            rightLeg.xRot = -1.4137167F;
            rightLeg.yRot = Mth.PI / 10;
            rightLeg.zRot = 0.07853982F;
            leftLeg.xRot = -1.4137167F;
            leftLeg.yRot = -Mth.PI / 10;
            leftLeg.zRot = -0.07853982F;
        }

        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        boolean flag2 = entity.getMainArm() == HumanoidArm.RIGHT;
        if (entity.isUsingItem()) {
            boolean flag3 = entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
            if (flag3 == flag2) {
                poseRightArm(entity);
            } else {
                poseLeftArm(entity);
            }
        } else {
            boolean flag4 = flag2 ? leftArmPose.isTwoHanded() : rightArmPose.isTwoHanded();
            if (flag2 != flag4) {
                poseLeftArm(entity);
                poseRightArm(entity);
            } else {
                poseRightArm(entity);
                poseLeftArm(entity);
            }
        }

        setupAttackAnimation(entity, ageInTicks);
        if (crouching) {
            body.xRot = 0.5F;
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
            rightLeg.z = 4.0F;
            leftLeg.z = 4.0F;
            rightLeg.y = 12.2F;
            leftLeg.y = 12.2F;
            head.y = 4.2F;
            body.y = 3.2F;
            leftArm.y = 5.2F;
            rightArm.y = 5.2F;
        } else {
            body.xRot = 0.0F;
            rightLeg.z = 0.0F;
            leftLeg.z = 0.0F;
            rightLeg.y = 12.0F;
            leftLeg.y = 12.0F;
            head.y = 0.0F;
            body.y = 0.0F;
            leftArm.y = 2.0F;
            rightArm.y = 2.0F;
        }

        // Arm bobbing
        if (rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(rightArm, ageInTicks, 1.0F);
        }
        if (leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(leftArm, ageInTicks, -1.0F);
        }

        // Swimming
        if (swimAmount > 0.0F) {
            float swimLimbSwing = limbSwing % 26.0F;
            HumanoidArm attackArm = getAttackArm(entity);
            float adjustedSwimAmount = attackArm == HumanoidArm.RIGHT && attackTime > 0.0F ? 0.0F : swimAmount;
            float f2 = attackArm == HumanoidArm.LEFT && attackTime > 0.0F ? 0.0F : swimAmount;
            if (!entity.isUsingItem()) {
                if (swimLimbSwing < 14.0F) {
                    leftArm.xRot = rotlerpRad(f2, leftArm.xRot, 0.0F);
                    rightArm.xRot = Mth.lerp(adjustedSwimAmount, rightArm.xRot, 0.0F);
                    leftArm.yRot = rotlerpRad(f2, leftArm.yRot, Mth.PI);
                    rightArm.yRot = Mth.lerp(adjustedSwimAmount, rightArm.yRot, Mth.PI);
                    leftArm.zRot = rotlerpRad(
                            f2, leftArm.zRot, Mth.PI + 1.8707964F * quadraticArmUpdate(swimLimbSwing) / quadraticArmUpdate(14.0F)
                    );
                    rightArm.zRot = Mth.lerp(
                            adjustedSwimAmount, rightArm.zRot, Mth.PI - 1.8707964F * quadraticArmUpdate(swimLimbSwing) / quadraticArmUpdate(14.0F)
                    );
                } else if (swimLimbSwing >= 14.0F && swimLimbSwing < 22.0F) {
                    float f6 = (swimLimbSwing - 14.0F) / 8.0F;
                    leftArm.xRot = rotlerpRad(f2, leftArm.xRot, Mth.HALF_PI * f6);
                    rightArm.xRot = Mth.lerp(adjustedSwimAmount, rightArm.xRot, Mth.HALF_PI * f6);
                    leftArm.yRot = rotlerpRad(f2, leftArm.yRot, Mth.PI);
                    rightArm.yRot = Mth.lerp(adjustedSwimAmount, rightArm.yRot, Mth.PI);
                    leftArm.zRot = rotlerpRad(f2, leftArm.zRot, 5.012389F - 1.8707964F * f6);
                    rightArm.zRot = Mth.lerp(adjustedSwimAmount, rightArm.zRot, 1.2707963F + 1.8707964F * f6);
                } else if (swimLimbSwing >= 22.0F && swimLimbSwing < 26.0F) {
                    float f3 = (swimLimbSwing - 22.0F) / 4.0F;
                    leftArm.xRot = rotlerpRad(f2, leftArm.xRot, Mth.HALF_PI - Mth.HALF_PI * f3);
                    rightArm.xRot = Mth.lerp(adjustedSwimAmount, rightArm.xRot, Mth.HALF_PI - Mth.HALF_PI * f3);
                    leftArm.yRot = rotlerpRad(f2, leftArm.yRot, Mth.PI);
                    rightArm.yRot = Mth.lerp(adjustedSwimAmount, rightArm.yRot, Mth.PI);
                    leftArm.zRot = rotlerpRad(f2, leftArm.zRot, Mth.PI);
                    rightArm.zRot = Mth.lerp(adjustedSwimAmount, rightArm.zRot, Mth.PI);
                }
            }

            float legScale = 0.3F;
            float legSpeed = 0.33333334F;
            leftLeg.xRot = Mth.lerp(swimAmount, leftLeg.xRot, legScale * Mth.cos(limbSwing * legSpeed + Mth.PI));
            rightLeg.xRot = Mth.lerp(swimAmount, rightLeg.xRot, legScale * Mth.cos(limbSwing * legSpeed));
        }

        //// Deviation from Vanilla

        if (ageInTicks != 0.0F) { // ageInTicks equals 0 when rendering the first-person hand
            if (modelProperties.legs().isPresent()) {
                ModelProperties.PartProperties props = modelProperties.legs().get();

                float xAddition = 4 * (props.scaleX() - 1);
                leftLeg.x += xAddition * 0.5F;
                rightLeg.x -= xAddition * 0.5F;

                float y = 12 * (props.scaleY() - 1);
                leftLeg.y -= y * 0.5F;
                rightLeg.y -= y * 0.5F;
                body.y -= y;
                leftArm.y -= y;
                rightArm.y -= y;
                head.y -= y;
                cloak.y -= y;
            }

            if (modelProperties.arms().isPresent()) {
                ModelProperties.PartProperties props = modelProperties.arms().get();

                float x = (slim ? 3 : 4) * (props.scaleX() - 1);
                leftArm.x += x * 0.5F;
                rightArm.x -= x * 0.5F;
            }

            if (modelProperties.body().isPresent()) {
                ModelProperties.PartProperties props = modelProperties.body().get();

                float x = 8 * (props.scaleX() - 1);
                leftArm.x += x * 0.5F;
                rightArm.x -= x * 0.5F;

                float y = 12 * (props.scaleY() - 1);
                body.y -= y * 0.5F;
                leftArm.y -= y * 0.5F;
                rightArm.y -= y * 0.5F;
                head.y -= y;
                cloak.y -= y * 0.5F;
            }

            if (modelProperties.head().isPresent()) {
                ModelProperties.PartProperties props = modelProperties.head().get();

                float y = 8 * (props.scaleY() - 1);
                head.y -= y * 0.5F;
            }
        }

        //// From PlayerModel

        // Skin Overlay
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        jacket.copyFrom(body);
        hat.copyFrom(head);

        // Cape
        if (entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            if (entity.isCrouching()) {
                cloak.z = 1.4F;
                cloak.y = 1.85F;
            } else {
                cloak.z = 0.0F;
                cloak.y = 0.0F;
            }
        } else if (entity.isCrouching()) {
            cloak.z = 0.3F;
            cloak.y = 0.8F;
        } else {
            cloak.z = -1.1F;
            cloak.y = -0.85F;
        }
    }
}
