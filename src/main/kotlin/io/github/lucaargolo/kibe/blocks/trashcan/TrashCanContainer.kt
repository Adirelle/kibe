package io.github.lucaargolo.kibe.blocks.trashcan

import io.github.lucaargolo.kibe.blocks.TRASH_CAN
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.container.Slot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TrashCanContainer(syncId: Int, playerInventory: PlayerInventory, val entity: TrashCanEntity, private val blockContext: BlockContext): Container(null, syncId) {

    var inventory: Inventory = object: Inventory {
        override fun getInvSize(): Int {
            return entity.invSize
        }

        override fun isInvEmpty(): Boolean {
            return entity.isInvEmpty
        }

        override fun getInvStack(slot: Int): ItemStack? {
            return entity.getInvStack(slot)
        }

        override fun removeInvStack(slot: Int): ItemStack? {
            val stack: ItemStack = entity.removeInvStack(slot)
            onContentChanged(this)
            return stack
        }

        override fun takeInvStack(slot: Int, amount: Int): ItemStack? {
            val stack: ItemStack = entity.takeInvStack(slot, amount)
            onContentChanged(this)
            return stack
        }

        override fun setInvStack(slot: Int, stack: ItemStack?) {
            entity.setInvStack(slot, stack)
            onContentChanged(this)
        }

        override fun markDirty() {
            entity.markDirty()
        }

        override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
            return entity.canPlayerUseInv(player)
        }

        override fun clear() {
            entity.clear()
        }
    }

    init {
        checkContainerSize(inventory, 1)
        inventory.onInvOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(Slot(inventory, 0, 8 + 4*18,  36))

        (0..2).forEach {n ->
            (0..8).forEach { m ->
                addSlot(
                    Slot(
                        playerInventory,
                        m + n * 9 + 9,
                        8 + m * 18,
                        103 + n * 18 + i
                    )
                )
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
        }

    }

    override fun canUse(player: PlayerEntity): Boolean {
        return blockContext.run({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(
                    blockPos
                ).block != TRASH_CAN
            ) false else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 1) {
                if (!insertItem(itemStack2, 1, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

}