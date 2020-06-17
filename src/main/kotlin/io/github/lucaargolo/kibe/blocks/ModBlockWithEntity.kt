package io.github.lucaargolo.kibe.blocks

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "unused")
class ModBlockWithEntity<T: BlockEntity>(block: BlockWithEntity, renderer: KClass<*>?, container: KClass<*>?, screen: KClass<*>?, customBlockItem: KClass<*>?) : ModBlock(block) {

    var entity: BlockEntityType<T>? = null
        private set
    private var renderer: KClass<BlockEntityRenderer<T>>? = renderer as KClass<BlockEntityRenderer<T>>?
    private var container: KClass<Container>? = container as KClass<Container>?
    private var containerScreen: KClass<ContainerScreen<*>>? = screen as KClass<ContainerScreen<*>>?
    private var customBlockItem: KClass<BlockItem>? = customBlockItem as KClass<BlockItem>?

    init {
        this.entity = BlockEntityType.Builder.create(Supplier { block.createBlockEntity(null) }, block).build(null) as BlockEntityType<T>
    }

    override fun init(identifier: Identifier) {
        if(this.customBlockItem != null) {
            Registry.register(Registry.BLOCK, identifier, block)
            val blockItem = customBlockItem!!.java.constructors[0].newInstance(block, Item.Settings()) as BlockItem
            //val blockItem = customBlockItem!!.primaryConstructor!!.call(block, Item.Settings())
            Registry.register(Registry.ITEM, identifier, blockItem)
        }else{
            super.init(identifier)
        }
        if (entity != null) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, entity)
        }
        if (container != null) {
            ContainerProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                val pos = packetByteBuf.readBlockPos()
                container!!.java.constructors[0].newInstance(syncId,
                    playerEntity.inventory,
                    playerEntity.world.getBlockEntity(pos),
                    BlockContext.create(playerEntity.world, pos)
                ) as Container
//                container!!.primaryConstructor!!.call(syncId,
//                    playerEntity.inventory,
//                    playerEntity.world.getBlockEntity(pos),
//                    ScreenHandlerContext.create(playerEntity.world, pos)
//                )
            }
        }
    }

    override fun initClient(identifier: Identifier) {
        super.initClient(identifier)
        if(containerScreen != null) {
            ScreenProviderRegistry.INSTANCE.registerFactory(identifier) { syncId: Int, _, playerEntity: PlayerEntity, packetByteBuf: PacketByteBuf ->
                val pos = packetByteBuf.readBlockPos()
                val entity = playerEntity.entityWorld.getBlockEntity(pos) as LockableContainerBlockEntity
                containerScreen!!.java.constructors[0].newInstance(
                    container!!.java.constructors[0].newInstance(
                        syncId,
                        playerEntity.inventory,
                        entity,
                        BlockContext.EMPTY
                    ) as Container, playerEntity.inventory, entity.name
                ) as ContainerScreen<*>
//                containerScreen!!.primaryConstructor!!.call(
//                    container!!.primaryConstructor!!.call(
//                        syncId,
//                        playerEntity.inventory,
//                        entity,
//                        ScreenHandlerContext.EMPTY
//                    ), playerEntity.inventory, entity.name
//                )
            }
        }
        if(renderer != null) {
            BlockEntityRendererRegistry.INSTANCE.register(entity) { it2 ->
                //renderer!!.primaryConstructor!!.call(it2)
                renderer!!.java.constructors[0].newInstance(it2) as BlockEntityRenderer<T>
            }
        }
    }
}