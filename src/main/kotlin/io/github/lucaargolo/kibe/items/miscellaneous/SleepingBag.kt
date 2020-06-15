package io.github.lucaargolo.kibe.items.miscellaneous

import com.mojang.datafixers.util.Either
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.Unit
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class SleepingBag(settings: Settings): Item(settings) {

    companion object {
        val playersSleeping = mutableListOf<PlayerEntity>()
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) {
            customTrySleep(player).ifLeft { sleepFailureReason ->
                if (sleepFailureReason != null) {
                    player.sendMessage(sleepFailureReason.toText())
                }
            }
        }
        return TypedActionResult.success(player.getStackInHand(hand))
    }

    private fun isSleepingBagObstructed(world: World, pos: BlockPos, direction: Direction): Boolean {
        val blockPos: BlockPos = pos.up()
        return world.getBlockState(blockPos).canSuffocate(world, blockPos) || world.getBlockState(blockPos.offset(direction.opposite)).canSuffocate(world, blockPos.offset(direction.opposite))
    }

    private fun customTrySleep(player: PlayerEntity): Either<PlayerEntity.SleepFailureReason, Unit> {
        val rayTraceContext = player.rayTrace(4.5, 1.0f, false)
        val sleepingPos = rayTraceContext.run {
            if(this.type == HitResult.Type.BLOCK) {
                return@run BlockPos(this.pos)
            }else{
                return@run BlockPos.ORIGIN
            }
        }
        if (player.isSleeping || !player.isAlive) {
            return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM)
        }
        if (!player.world.dimension.hasSkyLight()) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE)
        }
        if (player.world.isDay) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW)
        }
        if (isSleepingBagObstructed(player.world, sleepingPos, player.horizontalFacing)) {
            return Either.left(PlayerEntity.SleepFailureReason.OBSTRUCTED)
        }
        if (!player.isCreative) {
            val vec3d = player.pos
            val list = player.world.getEntities(
                HostileEntity::class.java,
                Box(
                    vec3d.getX() - 8.0,
                    vec3d.getY() - 5.0,
                    vec3d.getZ() - 8.0,
                    vec3d.getX() + 8.0,
                    vec3d.getY() + 5.0,
                    vec3d.getZ() + 8.0
                )
            ) { hostileEntity: HostileEntity -> hostileEntity.isAngryAt(player) }
            if (list.isNotEmpty()) {
                return Either.left(PlayerEntity.SleepFailureReason.NOT_SAFE)
            }
        }

        playersSleeping.add(player)
        player.sleep(sleepingPos)
        (player.world as ServerWorld).updatePlayersSleeping()

        return Either.right(Unit.INSTANCE)
    }

}