package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.getEntityType
import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*

class VacuumHopper: BlockWithEntity(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL).nonOpaque()) {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return VacuumHopperEntity(this, blockPos, blockState)
    }

    override fun <T : BlockEntity?> getTicker(world: World?, blockState: BlockState?, blockEntityType: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(blockEntityType, getEntityType(this)) { wrld, pos, state, blockEntity -> VacuumHopperEntity.tick(wrld, pos, state, blockEntity as VacuumHopperEntity) }
    }

    init {
        defaultState = stateManager.defaultState.with(Properties.ENABLED, true)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.ENABLED)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.ENABLED, !ctx.world.isReceivingRedstonePower(ctx.blockPos))
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos?, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        if (!world.isClient) {
            val isEnabled = state[Properties.ENABLED]
            if (isEnabled == world.isReceivingRedstonePower(pos)) {
                if (isEnabled) world.blockTickScheduler.schedule(pos, this, 4)
                else world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
            }
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos?, random: Random?) {
        if (state[Properties.ENABLED] && world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(Properties.ENABLED), 2)
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos?, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity as Inventory?)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, notify)
        }
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if(state[Properties.ENABLED]) {
            repeat(4) {
                var x = pos.x.toDouble() + random.nextDouble()
                val y = pos.y.toDouble() + random.nextDouble()
                var z = pos.z.toDouble() + random.nextDouble()

                var velocityX = (random.nextFloat().toDouble() - 0.5) * 0.5
                val velocityY = (random.nextFloat().toDouble() - 0.5) * 0.5
                var velocityZ = (random.nextFloat().toDouble() - 0.5) * 0.5

                val k = random.nextInt(2) * 2 - 1
                if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
                    x = pos.x.toDouble() + 0.5 + 0.25 * k.toDouble()
                    velocityX = (random.nextFloat() * 2.0f * k.toFloat()).toDouble()
                } else {
                    z = pos.z.toDouble() + 0.5 + 0.25 * k.toDouble()
                    velocityZ = (random.nextFloat() * 2.0f * k.toFloat()).toDouble()
                }

                world.addParticle(ParticleTypes.PORTAL, x, y, z, velocityX, velocityY, velocityZ)
            }
        }
    }

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        var output = 0
        (world.getBlockEntity(pos) as? VacuumHopperEntity)?.let { entity ->
            var i = 0
            var f = 0.0f
            repeat(9) {
                val itemStack: ItemStack = entity.getStack(it)
                if (!itemStack.isEmpty) {
                    f += itemStack.count.toFloat() / entity.maxCountPerStack.coerceAtMost(itemStack.maxCount).toFloat()
                    ++i
                }
            }
            output = MathHelper.floor((f/9f) * 14.0f) + if (i > 0) 1 else 0
        }
        return output
    }

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean {
        return true
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

}