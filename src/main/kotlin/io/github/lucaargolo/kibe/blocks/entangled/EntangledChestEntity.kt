package io.github.lucaargolo.kibe.blocks.entangled

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.DefaultedList
import net.minecraft.util.DyeColor

class EntangledChestEntity(chest: EntangledChest): LockableContainerBlockEntity(getEntityType(chest)), BlockEntityClientSerializable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var runeColors = mutableMapOf<Int, DyeColor>()
    var key = EntangledChest.DEFAULT_KEY
    var owner = ""

    init {
        (1..8).forEach {
            runeColors[it] = DyeColor.WHITE
        }
    }

    fun getColorCode(): String {
        var code = ""
        (1..8).forEach {
            code += when(runeColors[it]!!) {
                DyeColor.WHITE -> '0'
                DyeColor.ORANGE -> '1'
                DyeColor.MAGENTA -> '2'
                DyeColor.LIGHT_BLUE -> '3'
                DyeColor.YELLOW -> '4'
                DyeColor.LIME -> '5'
                DyeColor.PINK -> '6'
                DyeColor.GRAY -> '7'
                DyeColor.LIGHT_GRAY -> '8'
                DyeColor.CYAN -> '9'
                DyeColor.BLUE -> 'A'
                DyeColor.PURPLE -> 'B'
                DyeColor.GREEN -> 'C'
                DyeColor.BROWN -> 'D'
                DyeColor.RED -> 'E'
                DyeColor.BLACK -> 'F'
            }
        }
        return code
    }

    override fun createContainer(i: Int, playerInventory: PlayerInventory?): Container? {
        return null
    }

    private fun hasPersistentState(): Boolean = hasWorld() && !world!!.isClient

    private fun getPersistentState(): EntangledChestState? {
        return if(hasWorld() && !world!!.isClient) {
            (world as ServerWorld).persistentStateManager.getOrCreate( {EntangledChestState(key)}, key)
        }else null
    }

    override fun markDirty() {
        if(hasPersistentState()) {
             getPersistentState()!!.markDirty()
        }
        super.markDirty()
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
        if(hasPersistentState()) {
            val subTag = CompoundTag()
            subTag.put(getColorCode(), tag)
            getPersistentState()!!.fromTag(subTag)
        }
        else {
            this.inventory = DefaultedList.ofSize(this.invSize, ItemStack.EMPTY)
            Inventories.fromTag(tag, this.inventory)
        }
    }

    override fun fromClientTag(tag: CompoundTag) {
        (1..8).forEach {
            runeColors[it] = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
        }
        key = tag.getString("key")
        owner = tag.getString("owner")
        this.inventory = DefaultedList.ofSize(this.invSize, ItemStack.EMPTY)
        Inventories.fromTag(tag, this.inventory)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        if(hasPersistentState()) {
            var subTag = CompoundTag()
            subTag = getPersistentState()!!.toTag(subTag)
            if(subTag[getColorCode()] != null) {
                subTag = subTag.get(getColorCode()) as CompoundTag
                tag.put("Items", subTag.get("Items"))
            }
        }
        else Inventories.toTag(tag, this.inventory)
        return tag
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        (1..8).forEach {
            tag.putString("rune$it", runeColors[it]!!.getName())
        }
        tag.putString("key", key)
        tag.putString("owner", owner)
        Inventories.toTag(tag, this.inventory)
        return tag
    }

    override fun getInvSize(): Int {
        return if(hasPersistentState()) getPersistentState()!!.size(getColorCode())
        else inventory.size
    }

    override fun isInvEmpty(): Boolean {
        return if(hasPersistentState()) getPersistentState()!!.isEmpty(getColorCode())
        else {
            val iterator = this.inventory.iterator()
            var itemStack: ItemStack
            do {
                if (iterator.hasNext())
                    return true
                itemStack = iterator.next()
            } while(itemStack.isEmpty)
            return false
        }
    }

    override fun getInvStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.getStack(slot, getColorCode())
        else inventory[slot]
    }

    override fun takeInvStack(slot: Int, amount: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, amount, getColorCode())
        else Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeInvStack(slot: Int): ItemStack {
        return if(hasPersistentState()) getPersistentState()!!.removeStack(slot, getColorCode())
        else Inventories.removeStack(this.inventory, slot)
    }

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        if(hasPersistentState()) getPersistentState()!!.setStack(slot, stack, getColorCode())
        else {
            inventory[slot] = stack
            if (stack!!.count > invMaxStackAmount) {
                stack.count = invMaxStackAmount
            }
        }
    }

    override fun clear() {
        return if(hasPersistentState()) getPersistentState()!!.clear(getColorCode())
        else inventory.clear()
    }

    override fun getContainerName(): Text = TranslatableText("screen.kibe.entangled_chest")

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }




}