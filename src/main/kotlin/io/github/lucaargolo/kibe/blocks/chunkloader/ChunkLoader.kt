package io.github.lucaargolo.kibe.blocks.chunkloader

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.inventory.Inventory
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ChunkLoader: BlockWithEntity(FabricBlockSettings.of(Material.STONE).strength(22.0F, 600.0F)) {

    override fun createBlockEntity(world: BlockView?) = ChunkLoaderBlockEntity(this)

    override fun getRenderType(state: BlockState?) =  BlockRenderType.MODEL

    override fun onBlockRemoved(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (state.block !== newState.block) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is ChunkLoaderBlockEntity) {
                val chunkLoaderState = (world as ServerWorld).persistentStateManager.getOrCreate({ ChunkLoaderState(world.server, "kibe:chunk_loaders") }, "kibe:chunk_loaders")
                chunkLoaderState.removePos(pos, world)
            }
            super.onBlockRemoved(state, world, pos, newState, moved)
        }
    }

    override fun getCollisionShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape = VoxelShapes.union(
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        createCuboidShape(3.0, 12.0, 3.0, 13.0, 13.0, 13.0)
    )

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape = VoxelShapes.union(
        createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        createCuboidShape(3.0, 12.0, 3.0, 13.0, 13.0, 13.0)
    )
    
}