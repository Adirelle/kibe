package io.github.lucaargolo.kibe.items.entangled

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestState
import net.minecraft.container.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DefaultedList
import net.minecraft.world.World

class EntangledBagContainer(syncId: Int, playerInventory: PlayerInventory, val world: World, val tag: CompoundTag): Container(null, syncId) {

    private fun hasPersistentState(): Boolean = !world.isClient

    private fun getPersistentState(): EntangledChestState? {
        return if(!world.isClient) {
            (world as ServerWorld).persistentStateManager.getOrCreate( { EntangledChestState(key) }, key)
        }else null
    }

    val key: String = tag.getString("key")
    val colorCode: String = tag.getString("colorCode")
    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)

    var synchronizedInventory: Inventory = object: Inventory {
        
        override fun getInvSize(): Int {
            return if(hasPersistentState()) getPersistentState()!!.getInvSize(colorCode);
            else inventory.size;
        }

        override fun isInvEmpty(): Boolean {
            return if(hasPersistentState()) getPersistentState()!!.isInvEmpty(colorCode);
            else {
                val iterator = inventory.iterator()
                var itemStack: ItemStack
                do {
                    if (iterator.hasNext())
                        return true;
                    itemStack = iterator.next()
                } while(itemStack.isEmpty)
                return false
            }
        }

        override fun getInvStack(slot: Int): ItemStack {
            return if(hasPersistentState()) getPersistentState()!!.getInvStack(slot, colorCode);
            else inventory[slot]
        }

        override fun markDirty() {
            if(hasPersistentState()) {
                getPersistentState()!!.markDirty()
            }
        }

        override fun takeInvStack(slot: Int, amount: Int): ItemStack {
            return if(hasPersistentState()) getPersistentState()!!.takeInvStack(slot, amount, colorCode);
            else Inventories.splitStack(inventory, slot, amount)
        }

        override fun removeInvStack(slot: Int): ItemStack {
            return if(hasPersistentState()) getPersistentState()!!.removeInvStack(slot, colorCode);
            else Inventories.removeStack(inventory, slot);
        }

        override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
            return true
        }

        override fun setInvStack(slot: Int, stack: ItemStack?) {
            if(hasPersistentState()) getPersistentState()!!.setInvStack(slot, stack, colorCode);
            else {
                inventory[slot] = stack
                if (stack!!.count > invMaxStackAmount) {
                    stack.count = invMaxStackAmount
                }
            }
        }

        override fun clear() {
            return if(hasPersistentState()) getPersistentState()!!.clear(colorCode);
            else inventory.clear()
        }
    }

    init {
        checkContainerSize(synchronizedInventory, 27)
        synchronizedInventory.onInvOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        var n: Int
        var m: Int

        n = 0
        while (n < 3) {
            m = 0
            while (m < 9) {
                addSlot(Slot(synchronizedInventory, m + n * 9, 8 + m * 18, 18 + n * 18))
                ++m
            }
            ++n
        }

        n = 0
        while (n < 3) {
            m = 0
            while (m < 9) {
                addSlot(
                    Slot(
                        playerInventory,
                        m + n * 9 + 9,
                        8 + m * 18,
                        103 + n * 18 + i
                    )
                )
                ++m
            }
            ++n
        }

        n = 0
        while (n < 9) {
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
            ++n
        }

    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 27) {
                if (!insertItem(itemStack2, 27, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 27, false)) {
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