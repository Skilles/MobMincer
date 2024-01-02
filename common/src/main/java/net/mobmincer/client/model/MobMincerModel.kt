package net.mobmincer.client.model

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
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
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.entity.MobMincerEntity

class MobMincerModel(private val root: ModelPart) : HierarchicalModel<MobMincerEntity>() {
    private val head: ModelPart = root.getChild("head")
    private val legs: ModelPart = root.getChild("legs")
    private val chest: ModelPart = root.getChild("chest")
    private val spreader: ModelPart = root.getChild("spreader")
    private val feeder: ModelPart = root.getChild("feeder")
    private val defaultTop: ModelPart = root.getChild("default_top")
    private val pinkTop: ModelPart = root.getChild("pink_top")

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
        /*if (Minecraft.getInstance().isPaused) {
            return
        }
        this.animate(entity.idleAnimationState, IDLE, entity.tickCount + partialTicks)*/
        val rotYRadians = rotY * (Math.PI.toFloat() / 180)
        val rotXRadians = rotX * (Math.PI.toFloat() / 180)
        head.yRot = rotYRadians
        head.xRot = rotXRadians
        legs.yRot = rotYRadians
        defaultTop.xRot = rotXRadians
        pinkTop.xRot = rotXRadians
        chest.xRot = rotXRadians
        spreader.xRot = rotXRadians
        defaultTop.yRot = rotYRadians
        pinkTop.yRot = rotYRadians
        chest.yRot = rotYRadians
        spreader.yRot = rotYRadians
        feeder.yRot = rotYRadians
        defaultTop.visible = true
        chest.visible = false
        spreader.visible = false
        feeder.visible = false
        pinkTop.visible = false
        if (entity.attachments.hasAttachment(Attachments.STORAGE)) {
            chest.visible = true
        }
        if (entity.attachments.hasAttachment(Attachments.PACIFIER)) {
            defaultTop.visible = false
            pinkTop.visible = true
        }
        if (entity.attachments.hasAttachment(Attachments.SPREADER)) {
            spreader.visible = true
        }
        if (entity.attachments.hasAttachment(Attachments.FEEDER)) {
            feeder.visible = true
        }
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
        chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
        spreader.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
        feeder.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
        defaultTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
        pinkTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha)
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

            val feeder = partdefinition.addOrReplaceChild(
                "feeder",
                CubeListBuilder.create().texOffs(
                    0,
                    20
                ).addBox(-4.25f, 0.8333f, -1.0f, 1.0f, 2.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val spreader = partdefinition.addOrReplaceChild(
                "spreader",
                CubeListBuilder.create().texOffs(
                    8,
                    15
                ).addBox(-2.5f, -4.6667f, -0.5f, 0.0f, 4.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(4, 16).addBox(-3.0f, -5.6667f, -0.5f, 1.0f, 1.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val default_top = partdefinition.addOrReplaceChild(
                "default_top",
                CubeListBuilder.create().texOffs(
                    18,
                    10
                ).addBox(-1.0f, -3.6667f, -1.0f, 2.0f, 1.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val pink_top = partdefinition.addOrReplaceChild(
                "pink_top",
                CubeListBuilder.create().texOffs(
                    0,
                    16
                ).addBox(-1.0f, -3.6667f, -1.0f, 2.0f, 2.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val head = partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(
                    4,
                    2
                ).addBox(-3.0f, -1.6667f, -3.0f, 6.0f, 1.0f, 6.0f, CubeDeformation(0.0f))
                    .texOffs(8, 3).addBox(-2.0f, -2.6667f, -2.0f, 4.0f, 1.0f, 4.0f, CubeDeformation(0.0f))
                    .texOffs(0, 0).addBox(-4.0f, -0.6667f, -4.0f, 8.0f, 8.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val legs = partdefinition.addOrReplaceChild(
                "legs",
                CubeListBuilder.create(),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            val cube_r1 = legs.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create().texOffs(
                    17,
                    6
                ).mirror().addBox(2.0f, 0.75f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(
                        16,
                        0
                    ).mirror().addBox(2.0f, 0.75f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(
                        0,
                        7
                    ).mirror().addBox(2.0f, 0.75f, 3.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(4.2426f, 4.7475f, -2.0f, 0.0f, 0.0f, 0.3927f)
            )

            val cube_r2 = legs.addOrReplaceChild(
                "cube_r2",
                CubeListBuilder.create().texOffs(
                    17,
                    6
                ).mirror().addBox(1.0f, 0.0f, -2.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(
                        17,
                        7
                    ).mirror().addBox(1.0f, 0.0f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(
                        17,
                        0
                    ).mirror().addBox(1.0f, 0.0f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 3.3333f, 0.0f, 0.0f, 0.0f, -0.7854f)
            )

            val cube_r3 = legs.addOrReplaceChild(
                "cube_r3",
                CubeListBuilder.create().texOffs(
                    17,
                    6
                ).addBox(-3.0f, 0.0f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-3.0f, 0.0f, -2.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-3.0f, 0.0f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 3.3333f, 0.0f, 0.0f, 0.0f, 0.7854f)
            )

            val cube_r4 = legs.addOrReplaceChild(
                "cube_r4",
                CubeListBuilder.create().texOffs(
                    18,
                    7
                ).addBox(-4.0f, 0.75f, 3.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(17, 5).addBox(-4.0f, 0.75f, 1.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f))
                    .texOffs(9, 0).addBox(-4.0f, 0.75f, -0.5f, 2.0f, 8.0f, 1.0f, CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.2426f, 4.7475f, -2.0f, 0.0f, 0.0f, -0.3927f)
            )

            val chest = partdefinition.addOrReplaceChild(
                "chest",
                CubeListBuilder.create().texOffs(
                    8,
                    20
                ).addBox(-3.0f, 0.3333f, 2.5f, 6.0f, 6.0f, 2.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 9.6667f, 0.0f)
            )

            return LayerDefinition.create(meshdefinition, 32, 32)
        }

        private val IDLE: AnimationDefinition = AnimationDefinition.Builder
            .withLength(2.0f)
            .looping()
            .addAnimation(
                "head",
                AnimationChannel(
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
