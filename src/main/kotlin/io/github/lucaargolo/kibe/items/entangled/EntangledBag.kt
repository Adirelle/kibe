package io.github.lucaargolo.kibe.items.entangled

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChest
import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestBlockItem
import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestEntity
import io.github.lucaargolo.kibe.blocks.vacuum.VacuumHopperEntity
import io.github.lucaargolo.kibe.items.getItemId
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import net.minecraft.util.*
import net.minecraft.world.World
import java.util.*

class EntangledBag(settings: Settings): Item(settings){

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val tag = getTag(stack)
        val ownerText = TranslatableText("tooltip.kibe.owner")
        if(tag.getString("key") != EntangledChest.DEFAULT_KEY) tooltip.add(ownerText.append(LiteralText(tag.getString("owner")).formatted(Formatting.GRAY)))
        val color = TranslatableText("tooltip.kibe.color")
        (1..8).forEach {
            val dc = DyeColor.byName(tag.getString("rune$it"), DyeColor.WHITE)
            val text = LiteralText("${EntangledChestBlockItem.getFormattingFromDye(dc)}■")
            color.append(text)
        }
        tooltip.add(color)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if(context.world.getBlockState(context.blockPos).block is EntangledChest && context.player != null && context.player!!.isSneaking) {
            val blockEntity = (context.world.getBlockEntity(context.blockPos) as EntangledChestEntity)
            val blockEntityTag = blockEntity.toTag(CompoundTag())
            val newTag = CompoundTag()
            newTag.putString("key", blockEntityTag.getString("key"))
            newTag.putString("owner", blockEntityTag.getString("owner"))
            (1..8).forEach {
                newTag.putString("rune$it", blockEntityTag.getString("rune$it"))
            }
            newTag.putString("colorCode", blockEntity.getColorCode())
            context.stack.tag = newTag
            if(!context.world.isClient) context.player!!.sendMessage(TranslatableText("chat.kibe.entangled_bag.success"))
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    fun getTag(stack: ItemStack): CompoundTag {
        return if(stack.hasTag()) {
            stack.orCreateTag
        }else{
            val newTag = CompoundTag()
            newTag.putString("key", EntangledChest.DEFAULT_KEY)
            (1..8).forEach {
                newTag.putString("rune$it", DyeColor.WHITE.name)
            }
            newTag.putString("colorCode", "00000000")
            newTag
        }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if(!world.isClient) {
            val tag = getTag(player.getStackInHand(hand))
            ContainerProviderRegistry.INSTANCE.openContainer(getItemId(this), player as ServerPlayerEntity?) { buf -> buf.writeCompoundTag(tag) }
        }
        return TypedActionResult.success(player.getStackInHand(hand))
    }


}