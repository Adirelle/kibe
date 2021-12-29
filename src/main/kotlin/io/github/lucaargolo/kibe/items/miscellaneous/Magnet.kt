package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.TRINKET
import io.github.lucaargolo.kibe.compat.trinkets.TrinketMagnet
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

open class Magnet(settings: Settings) : BooleanItem(settings) {

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        val player = entity as? PlayerEntity ?: return
        if(isEnabled(stack)) {
            val pos = player.blockPos
            val pos1 = BlockPos(pos.x - 8, pos.y - 8, pos.z - 8)
            val pos2 = BlockPos(pos.x + 8, pos.y + 8, pos.z + 8)
            val vecPos = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
            val validEntities = world.getOtherEntities(null, Box(pos1, pos2)) { it is ItemEntity || it is ExperienceOrbEntity }
            validEntities.forEach {
                val vel = it.pos.relativize(vecPos).normalize().multiply(0.1)
                it.addVelocity(vel.x, vel.y, vel.z)
            }
        }
    }

    companion object {
        fun create(settings: Settings): Magnet = if (TRINKET) TrinketMagnet(settings) else Magnet(settings)
    }
}