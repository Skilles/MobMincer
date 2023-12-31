package net.mobmincer.core.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.phys.EntityHitResult
import net.mobmincer.MobMincer
import org.joml.Quaternionf
import kotlin.math.atan2


class MobMincerEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<MobMincerEntity>(context) {
    private val model = MobMincerModel(context.bakeLayer(MobMincerModel.LAYER_LOCATION))

    override fun render(entity: MobMincerEntity, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int) {

        if (entity.isErrored) {
            val hitResult = Minecraft.getInstance().hitResult
            if (hitResult is EntityHitResult && (hitResult.entity == entity || hitResult.entity.id == entity.target.id)
            ) {
                renderErrorIcon(entity, poseStack, buffer, packedLight)
            }
        }

        renderMobMincer(entity, poseStack, buffer, packedLight, partialTicks)

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight)
    }

    private fun renderMobMincer(
        entity: MobMincerEntity,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        partialTicks: Float
    ) {
        poseStack.pushPose()
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        poseStack.translate(0.0, -1.0, 0.0)
        val rotY = Mth.rotLerp(partialTicks, entity.yRotO, entity.yRot)
        val rotX = Mth.lerp(partialTicks, entity.xRotO, entity.xRot)
        val vertexConsumer = buffer.getBuffer(model.renderType(this.getTextureLocation(entity)))
        model.setupAnim(entity, partialTicks, rotX, rotY)
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f)
        poseStack.popPose()
    }

    private fun renderErrorIcon(
        entity: MobMincerEntity,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        val cameraPos = this.entityRenderDispatcher.camera.position
        val entityPos = entity.position()

        poseStack.pushPose()
        poseStack.scale(0.25f, 0.25f, 0.25f)
        poseStack.translate(0.0, 3.5, 0.0)

        // Rotate the icon to face the camera
        val deltaYaw = atan2(cameraPos.z - entityPos.z, cameraPos.x - entityPos.x)
        val entityToCameraYaw = (Math.PI / 2) + deltaYaw
        val quaternion = Quaternionf().rotationY(-entityToCameraYaw.toFloat())
        poseStack.mulPose(quaternion)

        val matrix4f2 = poseStack.last().pose()
        val vertexConsumer2: VertexConsumer = buffer.getBuffer(Companion.MAP_ICONS)
        val b: Byte = MapDecoration.Type.RED_X.icon
        val g = (b % 16 + 0).toFloat() / 16.0f
        val h = (b / 16 + 0).toFloat() / 16.0f
        val l = (b % 16 + 1).toFloat() / 16.0f
        val m = (b / 16 + 1).toFloat() / 16.0f
        vertexConsumer2.vertex(matrix4f2, -1.0f, 1.0f, 0f)
            .color(255, 255, 255, 255)
            .uv(g, h)
            .uv2(packedLight)
            .endVertex()
        vertexConsumer2.vertex(matrix4f2, 1.0f, 1.0f, 0f)
            .color(255, 255, 255, 255)
            .uv(l, h)
            .uv2(packedLight)
            .endVertex()
        vertexConsumer2.vertex(matrix4f2, 1.0f, -1.0f, 0f)
            .color(255, 255, 255, 255)
            .uv(l, m)
            .uv2(packedLight)
            .endVertex()
        vertexConsumer2.vertex(matrix4f2, -1.0f, -1.0f, 0f)
            .color(255, 255, 255, 255)
            .uv(g, m)
            .uv2(packedLight)
            .endVertex()
        poseStack.popPose()
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
        val TEXTURE_LOCATION = ResourceLocation(MobMincer.MOD_ID, "textures/item/mob_mincer.png")
        private val MAP_ICONS_LOCATION = ResourceLocation("textures/map/map_icons.png")
        private val MAP_ICONS: RenderType = RenderType.text(MAP_ICONS_LOCATION)
    }
}
