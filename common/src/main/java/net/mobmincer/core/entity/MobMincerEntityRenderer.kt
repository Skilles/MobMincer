package net.mobmincer.core.entity

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.mobmincer.MobMincer

class MobMincerEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<MobMincerEntity>(context) {
    private val model = MobMincerModel(context.bakeLayer(MobMincerModel.LAYER_LOCATION))

    override fun render(entity: MobMincerEntity, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {
        poseStack.pushPose()
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        poseStack.translate(0.0, -1.0, 0.0)
        val f = Mth.rotLerp(partialTicks, entity.yRotO, entity.yRot)
        val g = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)
        val vertexConsumer = buffer.getBuffer(model.renderType(this.getTextureLocation(entity)))
        model.setupAnim(entity, partialTicks, g, f)
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f)
        poseStack.popPose()
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    override fun getTextureLocation(entity: MobMincerEntity): ResourceLocation {
        return TEXTURE_LOCATION
    }

    class Provider : EntityRendererProvider<MobMincerEntity> {
        override fun create(context: EntityRendererProvider.Context): EntityRenderer<MobMincerEntity> {
            return MobMincerEntityRenderer(context)
        }
    }

    companion object {
        private val TEXTURE_LOCATION = ResourceLocation(MobMincer.MOD_ID, "textures/mob_mincer.png")
    }
}
