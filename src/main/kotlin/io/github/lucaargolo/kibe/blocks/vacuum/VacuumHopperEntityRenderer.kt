package io.github.lucaargolo.kibe.blocks.vacuum

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Matrix4f
import java.util.*

class VacuumHopperEntityRenderer(private val arg: BlockEntityRendererFactory.Context): BlockEntityRenderer<VacuumHopperEntity> {

    private val random = Random(31100L)

    override fun render(blockEntity: VacuumHopperEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        if(blockEntity.cachedState[Properties.ENABLED] == true) {
            matrices.push()
            val m = matrices.peek().model
            renderMiddlePart(0.15f, m, vertexConsumers.getBuffer(RenderLayer.getEndPortal()))
            matrices.pop()
        }
    }

    private fun renderMiddlePart(g: Float, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        val red = (random.nextFloat() * 0.5f + 0.1f) * g
        val green = (random.nextFloat() * 0.5f + 0.4f) * g
        val blue = (random.nextFloat() * 0.5f + 0.5f) * g

        renderVertices(matrix4f, vertexConsumer, 0.02f, 0.98f, 0.02f, 0.98f, 0.98f, 0.98f, 0.98f, 0.98f, red, green, blue) //Direction.SOUTH
        renderVertices(matrix4f, vertexConsumer, 0.02f, 0.98f, 0.98f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, red, green, blue) //Direction.NORTH
        renderVertices(matrix4f, vertexConsumer, 0.98f, 0.98f, 0.98f, 0.02f, 0.02f, 0.98f, 0.98f, 0.02f, red, green, blue) //Direction.EAST
        renderVertices(matrix4f, vertexConsumer, 0.02f, 0.02f, 0.02f, 0.98f, 0.02f, 0.98f, 0.98f, 0.02f, red, green, blue) //Direction.WEST)
        renderVertices(matrix4f, vertexConsumer, 0.02f, 0.98f, 0.02f, 0.02f, 0.02f, 0.02f, 0.98f, 0.98f, red, green, blue) //Direction.DOWN
        renderVertices(matrix4f, vertexConsumer, 0.02f, 0.98f, 0.98f, 0.98f, 0.98f, 0.98f, 0.02f, 0.02f, red, green, blue) //Direction.UP
    }

    private fun renderVertices(matrix4f: Matrix4f, vertexConsumer: VertexConsumer, f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float, red: Float, green: Float, blue: Float) {
        vertexConsumer.vertex(matrix4f, f, h, j).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, g, h, k).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, g, i, l).color(red, green, blue, 1.0f).next()
        vertexConsumer.vertex(matrix4f, f, i, m).color(red, green, blue, 1.0f).next()
    }

}