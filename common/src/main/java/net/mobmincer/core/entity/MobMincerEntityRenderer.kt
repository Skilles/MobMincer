package net.mobmincer.core.entity

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.SkullModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

class MobMincerEntityRenderer protected constructor(context: EntityRendererProvider.Context) : EntityRenderer<MobMincerEntity>(context) {
    private val model = SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL))

    override fun render(entity: MobMincerEntity, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        poseStack.pushPose()
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        val f = Mth.rotLerp(partialTicks, entity.yRotO, entity.yRot)
        val g = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)
        val vertexConsumer = buffer.getBuffer(model.renderType(this.getTextureLocation(entity)))
        model.setupAnim(0.0f, f, g)
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f)
        poseStack.popPose()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    override fun getTextureLocation(entity: MobMincerEntity): ResourceLocation {
        return WITHER_LOCATION
    }

    class Provider : EntityRendererProvider<MobMincerEntity> {
        override fun create(context: EntityRendererProvider.Context): EntityRenderer<MobMincerEntity> {
            return MobMincerEntityRenderer(context)
        }
    }

    companion object {
        private val WITHER_LOCATION = ResourceLocation("textures/entity/wither/wither.png")
    }
}
