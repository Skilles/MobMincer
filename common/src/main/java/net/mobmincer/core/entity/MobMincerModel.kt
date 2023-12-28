package net.mobmincer.core.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.animation.AnimationChannel
import net.minecraft.client.animation.AnimationDefinition
import net.minecraft.client.animation.Keyframe
import net.minecraft.client.animation.KeyframeAnimations
import net.minecraft.client.model.HierarchicalModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.resources.ResourceLocation
import net.mobmincer.MobMincer


class MobMincerModel(private val root: ModelPart) : HierarchicalModel<MobMincerEntity>() {
    private val head: ModelPart = root.getChild("head")
    private val legs: ModelPart = root.getChild("legs")

    override fun setupAnim(
        entity: MobMincerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        setupAnim(entity, ageInTicks, headPitch, netHeadYaw)
    }

    fun setupAnim(entity: MobMincerEntity, partialTicks: Float, rotX: Float, rotY: Float) {
        if (Minecraft.getInstance().isPaused) {
            return
        }
        // this.animate(entity.idleAnimationState, IDLE, entity.tickCount + partialTicks)
        head.yRot = rotY * (Math.PI.toFloat() / 180)
        head.xRot = rotX * (Math.PI.toFloat() / 180)
        legs.yRot = rotY * (Math.PI.toFloat() / 180)
    }

    override fun renderToBuffer(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        packedLight: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
        legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
    }

    override fun root(): ModelPart {
        return root
    }

    companion object {
        // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(ResourceLocation(MobMincer.MOD_ID, "mob_mincer"), "main")

        fun createBodyLayer(): LayerDefinition {
            val meshdefinition = MeshDefinition()
            val partdefinition = meshdefinition.root

            val head = partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(4, 2)
                    .addBox(-3.0f, -4.875f, -3.0f, 6.0f, 1.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(9, 3).addBox(-2.0f, -5.875f, -2.0f, 4.0f, 1.0f, 4.0f, CubeDeformation(0.0f))
                    .texOffs(18, 10).addBox(-1.0f, -6.875f, -1.0f, 2.0f, 1.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0).addBox(-4.0f, -3.875f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 12.875f, 0.0f)
            )

            val legs =
                partdefinition.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0f, 16.0f, 0.0f))

            val cube_r1 = legs.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create().texOffs(17, 6).mirror()
                    .addBox(2.0f, 0.75f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)).mirror(false)
                    .texOffs(16, 0).mirror().addBox(2.0f, 0.75f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .mirror(false)
                    .texOffs(0, 7).mirror().addBox(2.0f, 0.75f, 3.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .mirror(false),
                PartPose.offsetAndRotation(4.2426f, -1.5858f, -2.0f, 0.0f, 0.0f, 0.3927f)
            )

            val cube_r2 = legs.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create().texOffs(17, 6).mirror()
                    .addBox(1.0f, 0.0f, -2.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)).mirror(false)
                    .texOffs(17, 7).mirror().addBox(1.0f, 0.0f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .mirror(false)
                    .texOffs(17, 0).mirror().addBox(1.0f, 0.0f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .mirror(false),
                PartPose.offsetAndRotation(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, -0.7854f)
            )

            val cube_r3 = legs.addOrReplaceChild(
                "cube_r3",
                CubeListBuilder.create().texOffs(17, 6)
                    .addBox(-3.0f, 0.0f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-3.0f, 0.0f, -2.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-3.0f, 0.0f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.7854f)
            )

            val cube_r4 = legs.addOrReplaceChild(
                "cube_r4",
                CubeListBuilder.create().texOffs(18, 7)
                    .addBox(-4.0f, 0.75f, 3.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-4.0f, 0.75f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(9, 0).addBox(-4.0f, 0.75f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.2426f, -1.5858f, -2.0f, 0.0f, 0.0f, -0.3927f)
            )

            return LayerDefinition.create(meshdefinition, 32, 16)
        }

        private val IDLE: AnimationDefinition = AnimationDefinition.Builder
            .withLength(2.0f)
            .looping()
            .addAnimation(
                "head", AnimationChannel(
                    AnimationChannel.Targets.ROTATION,
                    Keyframe(
                        0.0f,
                        KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f),
                        AnimationChannel.Interpolations.CATMULLROM
                    ),
                    Keyframe(
                        0.5f,
                        KeyframeAnimations.degreeVec(0.1f, 0.0f, 0.0f),
                        AnimationChannel.Interpolations.CATMULLROM
                    ),
                    Keyframe(
                        1.5f,
                        KeyframeAnimations.degreeVec(-0.1f, 0.0f, 0.0f),
                        AnimationChannel.Interpolations.CATMULLROM
                    ),
                    Keyframe(
                        2.0f,
                        KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f),
                        AnimationChannel.Interpolations.CATMULLROM
                    )
                )
            )
            .build()
    }
}
